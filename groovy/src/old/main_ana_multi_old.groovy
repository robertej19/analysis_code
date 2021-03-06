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
import groovy.json.JsonSlurper


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
def NumGlobalCDEvents = 0
def NumGlobalFDEvents = 0
def t_bins = [0.09,0.15,0.20,0.30,0.40,0.60,1.0,1.5,2,5]


//********************* Define Histograms **************** //



filename = "../../histogram_dict.json"
 
def jsonSlurper = new JsonSlurper()
def data = jsonSlurper.parse(new File(filename))
 
def histogram_array = []

for (hist in data){
    
    def hist_params = (hist.getValue()[0])

    def root_title= hist_params.get("root_title")
    def display_title = hist_params.get("display_title")
    def num_bins_x = hist_params.get("num_bins_x")
    def x_bin_min = hist_params.get("x_bin_min")
    def x_bin_max = hist_params.get("x_bin_max")
    def num_bins_z = hist_params.get("num_bins_z")
    def z_bin_min = hist_params.get("z_bin_min")
    def z_bin_max = hist_params.get("z_bin_max")


	//Split over 2D histogram or not
    if (num_bins_z > 0){
        def x2 = new H2F(root_title, display_title, num_bins_x, x_bin_min, x_bin_max, num_bins_z, z_bin_min, z_bin_max)
		histogram_array.add(x2)
    }
    else {
        def x1 = new H1F(root_title, display_title, num_bins_x, x_bin_min, x_bin_max)
		histogram_array.add(x1)
    }

	
    
}

def Hist_beta_p 	= [:].withDefault{new H2F("Hist_beta_p${it}"		, "Beta vs. Momentum ${it}"		          ,100,0,1.5,100,0,12)}
def Hist_beta_T 	= [:].withDefault{new H1F("Hist_beta_T${it}", "T in q2 xb bins of ${it}",50,0,5)}
def Hist_Ultra_Phi 	= [:].withDefault{new H1F("Hist_Ultra_Phi${it}", "Phi in in q2 xb t bins of ${it}",20,0,360)}

histogram_array.add(Hist_beta_p)
histogram_array.add(Hist_beta_T)
histogram_array.add(Hist_Ultra_Phi)

json_cuts_file = "../../cut_dict.json"
def cuts_json = jsonSlurper.parse(new File(json_cuts_file))
def cuts_array = []
for (cut_type in cuts_json){
	cuts_array.add(cut_type.getValue()[0])
}

print(cuts_array)


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
		def NumLocalFDEvents = 0
		def NumLocalCDEvents = 0
		def FileStartTime = new Date()
		println("\n \n \n \n Processing file $fname_short - ${(NumEventsToProcess/Mil).round(3)} M events events at ${FileStartTime.format('HH:mm:ss')}")


		
		// ******* Pass to event processor, increment variables of interest ****** //
		for (int j=0; j < NumEventsToProcess; j++) {
			evcount.getAndIncrement()
			su.UpdateScreen(FileStartTime.getTime(),evcount.get(),CountRate.toInteger(),NumEventsToProcess,fname_short)
			//println("event number: " + j)
			def event = reader.getNextEvent()
			funreturns = eventProcessor.processEvent(j,event,histogram_array,FCupCharge,cuts_array,t_bins)
			FCupCharge = funreturns[0]
			NumLocalDVPPEvents += funreturns[1]
			NumLocalFDEvents += funreturns[2]
			NumLocalCDEvents += funreturns[3]
			histogram_array = funreturns[4]
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

		NumGlobalCDEvents += NumLocalCDEvents
		NumGlobalFDEvents += NumLocalFDEvents
		NumGlobalDVPPEvents += NumLocalDVPPEvents
		GlobalLumiTotal += lumicalc.CalcLumi(FCupCharge)

		println("Global DVPP Events Found: $NumGlobalDVPPEvents, out of $GlobalNumEventsProcessed")
		println("Global FD Events Found: $NumGlobalFDEvents, compared to $NumGlobalCDEvents events in the CD")
		println("Charge on FCup from this run: $FCupCharge in nanoColoumbs")
		println("Total Integrated Luminosity so far is $GlobalLumiTotal-- UNITS???")

	}
}




//********* Output final running statistics *****************

def ScriptEndTime = new Date()
def ScriptRunTime = (ScriptEndTime.getTime() - ScriptStartTime.getTime())/1000/60 //Time to process file in minutes
def outputfilename = "${OutFileName}-${ScriptEndTime.format('YYYYMMdd-HH-mm')}"


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
println("File saved at ../hipo-root-files/${outputfilename}. \n")

//********* Save data in hipo and text files *****************


TDirectory out = new TDirectory()
out.mkdir('/'+OutFileName)
out.cd('/'+OutFileName)

def histos_json_out = histogram_array.dropRight(3) //Drops the last 3 elements in the array
histos_json_out.each { i ->
	out.addDataSet(i)
}

def binned_histograms = histogram_array.takeRight(3) //Takes the last 3 elements
def HistPB = binned_histograms[0]
def HistPT = binned_histograms[1]
def HistUPhi = binned_histograms[2]

def xb_bins = 10
for(int xBi=0;xBi<=12;xBi++){
	for(int q2i=0;q2i<=16;q2i++){
		def title = "${((xBi)/xb_bins).round(2)} < xB < ${((xBi+1)/xb_bins).round(2)}_ ${q2i/2+0.0} < q2 < ${q2i/2+0.5}"
		//printer("title is $title and q2i is $q2i with ${q2i/2}",2)
		out.addDataSet(HistPB[title])
		out.addDataSet(HistPT[title])
	}
}

for(int xBi=0;xBi<=12;xBi++){
	for(int q2i=0;q2i<=16;q2i++){
		for(int xti=0;xti<t_bins.size()-1;xti++){
			def title = "${((xBi)/xb_bins).round(2)} < xB < ${((xBi+1)/xb_bins).round(2)}_ ${q2i/2+0.0} < q2 < ${q2i/2+0.5}"
			def low = (t_bins[xti]).toFloat().round(3)
			def high = (t_bins[xti+1]).toFloat().round(3)
			def TitleUltra = title +" " + "$low < t <  $high"
			out.addDataSet(Hist_Ultra_Phi[TitleUltra])
		}
	}
}

out.writeFile("../hipo-root-files/${outputfilename}.hipo")

File file = new File("../hipo-root-files/${outputfilename}.txt")
file.append("Run information for ${outputfilename}.hipo \n")
file.append("Script began at ${ScriptStartTime.format('MM/dd/YYYY-HH:mm:ss')} and finished at ${ScriptEndTime.format('MM/dd/YYYY-HH:mm:ss')}\n")
if(ScriptRunTime > 1){
	file.append("total runtime: ${ScriptRunTime.round(2)} minutes - ")
}
else{
	file.append("total runtime: ${(ScriptRunTime*60).round(2)} seconds - ")
}
file.append("Processing rate: ${(GlobalNumEventsProcessed/ScriptRunTime/60/1000).round(2)} kHz \n")
if (NumGlobalDVPPEvents>0){
	file.append("Final global number of DVPP events found: $NumGlobalDVPPEvents out of a total of $GlobalNumEventsProcessed - ${(NumGlobalDVPPEvents/GlobalNumEventsProcessed*100).round(2)} %\n")
}
else{
	file.append("No DVPP events found out of a total of $GlobalNumEventsProcessed \n ")
}
if (NumGlobalCDEvents >0){
	file.append("Global FD Events Found: $NumGlobalFDEvents, compared to $NumGlobalCDEvents in the CD, a ratio of ${NumGlobalFDEvents/NumGlobalCDEvents*100} %")
}
else{
	file.append("No DVEP events found in CD, all (if any) DVEP events are in FD \n")
}	
file.append("Total Integrated Luminosity from the runs processed is $GlobalLumiTotal UNITS??? \n")
file.append("\n \n\n****************** \n The following pion cuts were used: \n")

for (cut_type in cuts_json){

	def cut_params = (cut_type.getValue()[0])

	if (cut_params.get("cut_class") == "DVEP"){
		file.append("\n ****** DVEP CUTS: \n \n")
		for(key in cut_params.keySet()){
			if (key != "cut_class"){
				file.append("Cut $key has value ${cut_params.get(key)} \n")
			}
		}
	}
	if (cut_params.get("cut_class") == "pions"){
		file.append("\n ****** PION CUTS: \n \n")
		for(key in cut_params.keySet()){
			if (key != "cut_class"){
				file.append("Cut $key has value ${cut_params.get(key)} \n")
			}
		}
	}


}

file.append("\n \n \n ***************** \n Used ${NumCores} cores to try to process ${NumFilesToProcess} files, processing a total of ${NumFilesProcessed} files. The following files were processed: \n")
for (filename in FilesToProcess){
	file.append("${filename} \n")
}

