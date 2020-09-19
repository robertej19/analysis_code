#!/usr/bin/groovy

//From Java
import java.io.*
import java.util.*
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.Date


//From JLab
import org.jlab.clas.physics.LorentzVector
import org.jlab.clas.physics.Vector3
import org.jlab.detector.base.DetectorType
import org.jlab.groot.base.GStyle
import org.jlab.groot.data.GraphErrors
import org.jlab.groot.data.H1F
import org.jlab.groot.data.H2F
import org.jlab.groot.data.TDirectory
import org.jlab.groot.fitter.DataFitter
import org.jlab.groot.graphics.EmbeddedCanvas
import org.jlab.groot.group.DataGroup
import org.jlab.groot.math.F1D
import org.jlab.io.base.DataBank
import org.jlab.io.base.DataEvent
import org.jlab.io.hipo.HipoDataSource
import org.jlab.io.hipo.HipoDataSync

//From Groovy
import groovyx.gpars.GParsPool


//From Local
import utils.FileGetter
import utils.Printer
import utils.EventProcessor
import utils.LumiCalc
import utils.ScreenUpdater

//****************** Initialize ********************** //
MyMods.enable() //I don't know what this does, its from Andrey, don't touch it, it works
ScriptStartTime = new Date()
println("\n \n Starting Groovy Script at ${ScriptStartTime.format('HH:mm:ss')} \n \n \n \n")

//********************* Read in command line arguements and initilaze globals and objets **************** //
if (args.size()<4) {
	println("You need to include the number of events and files you want to process in the start command, and number of cores!")
	println("THe first arg is the number of events per file to process, the second arg is the number of files to process, and the third is the number of cores")
	println("You can put a 0 for either files or number of events, this will result in all files / events being processed")
	println("For example, <run-groovy filename.groovy hipo_file_to_process.hipo 1000 10 3")
	println("Please enter this correctly, and try again. Exiting. \n \n")
	System.exit(0)
}

//*** Initialize objects for using methods *** //
def eventProcessor = new EventProcessor()
def fg = new FileGetter()
def lumicalc = new LumiCalc()
def su = new ScreenUpdater()
printerUtil = new Printer() //This needs to be defined as global (no def before the object name) so that it can be used in all methods


def DesiredNumEventsToProcess = args[1].toInteger()
def NumFilesToProcess = args[2].toInteger()
def NumCores = args[3].toInteger()
def FilesToProcess = fg.GetFile(args[0]).take(NumFilesToProcess) //args[0] is the path of the directory with all files to process

Mil = 1000000
def OutFileName = "output_file_histos"

def GlobalLumiTotal = 0
def GlobalNumEventsProcessed = 0
def GlobalRunTime = 0
def num_ep_events = 0
def NumGlobalDVPPEvents = 0
def NumFilesProcessed = 0
def GlobalFileSizeToProcess = 0
def GlobalFileSizeProcessed = 0

//********************* Define Histograms **************** //

//Number of particles per event
def hist_num_protons =  new H1F("hist_num_protons","Number of Protons per event",10,0,9)
def hist_num_photons_cut =  new H1F("hist_num_photons_cut","Number of Photons per event, 400 MeV cut",20,0,19)
def hist_num_photons_nocut =  new H1F("hist_num_photons_nocut","Number of Photons per event, no energy cut",20,0,19)


//Angle distribution: theta
def hist_theta_electron_no_cuts = new H1F("hist_theta_electron_no_cuts","Electron Theta Distribution",1500,0,50)

def hist_theta_proton_no_cuts = new H1F("hist_theta_proton_no_cuts","Proton Theta Distribution",1500,0,150)
def hist_theta_proton_FD_no_cuts = new H1F("hist_theta_proton_FD_no_cuts","Proton Theta Distribution in FD",1500,0,150)
def hist_theta_proton_CD_no_cuts = new H1F("hist_theta_proton_CD_no_cuts","Proton Theta Distribution in CD",1500,0,150)
def hist_theta_proton_FD_exclu_cuts = new H1F("hist_theta_proton_FD_exclu_cuts","Proton Theta Distribution in FD After Excl. Cuts",1500,0,150)
def hist_theta_proton_CD_exclu_cuts  = new H1F("hist_theta_proton_CD_exclu_cuts","Proton Theta Distribution in CD After Excl. Cuts",1500,0,150)

def hist_theta_proton_electron_no_cuts = new H2F("hist_theta_proton_electron_no_cuts","Proton vs. Electron Angles (Theta), No Cuts",800,0,150,800,0,55)
def hist_theta_proton_electron_FD_no_cuts = new H2F("hist_theta_proton_electron_FD_no_cuts","Proton vs. Electron Angles (Theta), FD, No Cuts",800,0,150,800,0,55)
def hist_theta_proton_electron_exclu_cuts = new H2F("hist_theta_proton_electron_exclu_cuts","Proton Angle vs. Electron Angle (Theta), Excl. Cuts",400,0,150,400,0,55)
def hist_theta_proton_electron_FD_exclu_cuts = new H2F("hist_theta_proton_electron_FD_exclu_cuts","Proton Angle vs. Electron Angle (Theta), FD, Excl. Cuts",400,0,150,400,0,55)

//Angle distribution: phi
def hist_phi_proton_nocuts = new H1F("hist_phi_proton_nocuts","Phi Distribution",380,-10,370)
def hist_phi_proton_nocuts_FD = new H1F("hist_phi_proton_nocuts_FD","Phi Distribution",380,-10,370)
def hist_phi_proton_nocuts_CD = new H1F("hist_phi_proton_nocuts_CD","Phi Distribution",380,-10,370)
def hist_phi_proton_excuts_CD = new H1F("hist_phi_proton_excuts_CD","Phi Distribution",380,-10,370)
def hist_phi_proton_excuts = new H1F("hist_phi_proton_excuts","Phi Distribution",380,-10,370)
def hist_phi_proton_excuts_FD = new H1F("hist_phi_proton_excuts_FD","Phi Distribution",380,-10,370)



//More advanced kinematic quantities
def hist_xB_nocuts = new H1F("hist_xB_nocuts","Bjorken x Distribution, No Cuts",1000,0,1.5)
def hist_xB_excuts = new H1F("hist_xB_excuts","Bjorken x Distribution, Excl. Cuts",100,0,1.5)
def hist_xB_Q2 = new H2F("hist_xB_Q2" , "Bjorken X vs. Q^2",300,0,1.5,300,0,12)
def hist_lept_had_angle = new H1F("hist_lept_had_angle" , "Angle Between Lepton and Hadron Planes",90,0,360)

def hist_Q2_nocuts = new H1F("hist_Q2_nocuts","Q^2 Distribution, no cuts",1000,0,12)
def hist_Q2_excuts = new H1F("hist_Q2_excuts","Q^2 Distribution, Excl. Cuts",1000,0,12)
def hist_W_nocuts = new H1F("hist_W_nocuts","W Distribution, no cuts",1000,0,12)
def hist_W_excuts = new H1F("hist_W_excuts","W Distribution, Excl. Cuts",1000,0,12)
def hist_helicity = new H1F("hist_helicity","Helicity",7,-2,2)

//Still more quantitites
def hist_t = new H1F("hist_t","Momentum transfer to Nucleon (t)",1000,-2,8)
def hist_t_recon = new H1F("hist_t_recon","Reconstructed Momentum transfer to Nucleon (t)",1000,-2,8)




def histogram_array = [hist_num_protons,hist_num_photons_cut,hist_num_photons_nocut,
						hist_theta_electron_no_cuts, hist_theta_proton_no_cuts, hist_theta_proton_CD_no_cuts, hist_theta_proton_FD_no_cuts,
						hist_theta_proton_FD_exclu_cuts, hist_theta_proton_CD_exclu_cuts,
						hist_theta_proton_electron_no_cuts,hist_theta_proton_electron_FD_no_cuts,
						hist_theta_proton_electron_exclu_cuts,hist_theta_proton_electron_FD_exclu_cuts,
						hist_xB_nocuts, hist_xB_excuts, hist_xB_Q2, hist_lept_had_angle,
						hist_Q2_nocuts, hist_Q2_excuts, hist_W_nocuts, hist_W_excuts, hist_helicity,
						hist_t, hist_t_recon,
						hist_phi_proton_nocuts, hist_phi_proton_nocuts_FD, hist_phi_proton_excuts, hist_phi_proton_excuts_FD,
						hist_phi_proton_nocuts_CD, hist_phi_proton_excuts_CD]


//********************* Display pre reunning statistics **************** //
printerUtil.printer("\n \nThe following files will be processed: ",1)
for (FileName in FilesToProcess){
	GlobalFileSizeToProcess+= FileName.length()
	printerUtil.printer("$FileName - " + (FileName.length()/Mil/1000).round(2)+"GB",2)
}
printerUtil.printer("\n" + (GlobalFileSizeToProcess/Mil/1000).round(2)+" GB is the total file size \n \n",2)



//********************* Process files **************** //
println("Starting to process files now: \n \n \n")


GParsPool.withPool NumCores, {
	FilesToProcess.eachParallel{fname->
		
		def reader = new HipoDataSource()
		reader.open(fname)

		def fname_short = fname.toString().split('/').last()

		def NumEventsInFile= reader.getSize().toInteger()
		def NumEventsToProcess = DesiredNumEventsToProcess
			if (DesiredNumEventsToProcess > NumEventsInFile){NumEventsToProcess = NumEventsInFile}
			if (DesiredNumEventsToProcess == 0){NumEventsToProcess = NumEventsInFile}
		def CountRate = NumEventsToProcess/2
			if (CountRate > 100000){CountRate = 100000} //If processing large number of events, update screen ever X events, where X is arbitrarily selected

		

		// *********** Define file specific stats *********
		def evcount = new AtomicInteger() // for counting how many events have been processed
		evcount.set(0)
		def Float FCupCharge = 0 //For counting charge on faraday cup
		def NumLocalDVPPEvents = 0
		def FileStartTime = new Date()
		println("\n \n \n \n Processing file $fname_short - ${(NumEventsToProcess/Mil).round(3)} M events events at ${FileStartTime.format('HH:mm:ss')}")


		
		// ******* Pass to event processor, increment variables of interest ****** //
		for (int j=0; j < NumEventsToProcess; j++) {
			evcount.getAndIncrement()
			su.UpdateScreen(FileStartTime.getTime(),evcount.get(),CountRate.toInteger(),NumEventsToProcess,fname_short)
			//println("event number: " + j)
			def event = reader.getNextEvent()
			funreturns = eventProcessor.processEvent(j,event,histogram_array,FCupCharge)
			FCupCharge = funreturns[0]
			NumLocalDVPPEvents += funreturns[1]
			histogram_array = funreturns[2]
		}

		reader.close()

		println("Num DVPP Events found in file $fname_short is $NumLocalDVPPEvents")

		// ******* Compile and print runtime statistics ****** //

		FileEndTime = new Date()
		def TotalFileRunTime = (FileEndTime.getTime() - FileStartTime.getTime())/1000/60 //Time to process file in minutes
		NumFilesProcessed++
		GlobalRunTime += TotalFileRunTime
		GlobalNumEventsProcessed += NumEventsToProcess
		GlobalFileSizeProcessed += fname.length() //This is a measure of how many bits have already been processed
		def TotalTimeLeft = GlobalRunTime*(GlobalFileSizeToProcess-GlobalFileSizeProcessed)/GlobalFileSizeProcessed //seconds per bit*bits left to process 
		def unixtimeETA = Math.round(TotalTimeLeft*60+FileEndTime.getTime()/1000) //add time left to current time
		def ReadableETA = Date.from(Instant.ofEpochSecond(unixtimeETA)).format('HH:mm:ss') //convert unix timestamp to readable time
		

		print("Finished processing ${(NumEventsToProcess/Mil).round(2)} M events at ${FileEndTime.format('HH:mm:ss')}, ")
		println("Processed $GlobalNumEventsProcessed events globally")
		if(TotalFileRunTime > 1){
			print("Total run time of ${TotalFileRunTime.round(2)} minutes, ")
		}
		else{
			print("Total run time of ${(TotalFileRunTime*60).round(2)} seconds, ")
		}
		printerUtil.printer(" approximate global finish time at ${ReadableETA} ",1)

		// ******* Compile and print Physics statistics ****** //

		NumGlobalDVPPEvents += NumLocalDVPPEvents
		GlobalLumiTotal += lumicalc.CalcLumi(FCupCharge)

		println("Global DVPP Events Found: $NumGlobalDVPPEvents, out of $GlobalNumEventsProcessed")
		println("Charge on FCup from this run: $FCupCharge in nanoColoumbs")
		println("Total Integrated Luminosity so far is $GlobalLumiTotal-- UNITS???")

	}
}



//********* Output final running statistics *****************

def ScriptEndTime = new Date()
def ScriptRunTime = (ScriptEndTime.getTime() - ScriptStartTime.getTime())/1000/60 //Time to process file in minutes
println("\n\n\n\nFinal Running Statistics:")
print("Script began at ${ScriptStartTime.format('HH:mm:ss')} and finished at ${ScriptEndTime.format('HH:mm:ss')}, ")
if(ScriptRunTime > 1){
	println("total runtime: ${ScriptRunTime.round(2)} minutes \n \n \n \n")
}
else{
	println("total runtime: ${(ScriptRunTime*60).round(2)} seconds \n \n \n \n")
}
println("Processed a total of $NumFilesProcessed files")
println("Final global number of DVPP events found: $NumGlobalDVPPEvents out of a total of $GlobalNumEventsProcessed")
println("Total Integrated Luminosity from the runs processed is $GlobalLumiTotal UNITS???")

//********* Save data in hipo and text files *****************

TDirectory out = new TDirectory()
out.mkdir('/'+OutFileName)
out.cd('/'+OutFileName)
histogram_array.each { i ->
	out.addDataSet(i)
}
out.writeFile("../hipo-root-files/${OutFileName}${ScriptEndTime.format('HH-mm')}.hipo")

File file = new File("../hipo-root-files/${OutFileName}${ScriptEndTime.format('HH-mm')}.txt")
file.append("Run information for ${OutFileName}${ScriptEndTime.format('HH-mm')}.hipo \n")
file.append("Script began at ${ScriptStartTime.format('MM/dd/YYYY-HH:mm:ss')} and finished at ${ScriptEndTime.format('MM/dd/YYYY-HH:mm:ss')}\n")
if(ScriptRunTime > 1){
	file.append("total runtime: ${ScriptRunTime.round(2)} minutes - ")
}
else{
	file.append("total runtime: ${(ScriptRunTime*60).round(2)} seconds - ")
}
file.append("Processing rate: ${(GlobalNumEventsProcessed/ScriptRunTime/60/1000).round(2)} kHz \n")
file.append("Final global number of DVPP events found: $NumGlobalDVPPEvents out of a total of $GlobalNumEventsProcessed - ${(NumGlobalDVPPEvents/GlobalNumEventsProcessed*100).round(2)} %\n")
file.append("Total Integrated Luminosity from the runs processed is $GlobalLumiTotal UNITS??? \n")
file.append("Used ${NumCores} cores to try to process ${NumFilesToProcess} files, processing a total of ${NumFilesProcessed} files. The following files were processed: \n")
for (filename in FilesToProcess){
	file.append("${filename} \n")
}

