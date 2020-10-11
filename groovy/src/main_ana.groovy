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
import java.text.DecimalFormat


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

println("arg size is "+ args)

//********************* Read in command line arguements and initilaze globals and objets **************** //
if (args.size()<5) {
	println("You need to include the number of events and files you want to process in the start command, and number of cores, and a run message!")
	println("THe first arg is the number of events per file to process, the second arg is the number of files to process, and the third is the number of cores, and fourth is the runtime message")
	println("You can put a 0 for either files or number of events, this will result in all files / events being processed")
	println('For example, <run-groovy filename.groovy hipo_file_to_process.hipo 1000 10 3 "testing new plotting mechanism"')
	println("Please enter this correctly, and try again. Exiting. \n \n")
	System.exit(0)
}

//*** Initialize objects for using methods *** //
def eventProcessor = new EventProcessor()
def fg = new FileGetter()
def lumicalc = new LumiCalc()
def su = new ScreenUpdater()
def jsonSlurper = new JsonSlurper()

def DesiredNumEventsToProcess = args[1].toInteger()
def NumFilesToProcess = args[2].toInteger()
def NumCores = args[3].toInteger()
def FilesToProcess = fg.GetFile(args[0]).take(NumFilesToProcess) //args[0] is the path of the directory with all files to process
def output_folder_groovy = args[4]
def output_message = ""

//Here we start at 4 beause the first 3 arguements are not the text string
//We need to convert the arguement array into one string, e.g. [this, is, the, message] -> "this is the message"
//There should be a cleaner way to do this, but this works
for (int ind=5; ind < args.size(); ind++) {
	output_message += args[ind] + " "
}

//def output_message = "pis are good"
println("output message is $output_message")

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
def NumGlobalCDDVEPEvents = 0
def NumGlobalFDDVEPEvents = 0
def NumGlobalCDAllEvents = 0
def NumGlobalFDAllEvents = 0


// ******************************************************************* //
// ****************** Read in infromation from json files ************ //
// ******************************************************************* //

// *********************** Define Binning **************************** //
file_loc_binning = "../../histogram_binning.json"

def data_binning = jsonSlurper.parse(new File(file_loc_binning))
 
def binning_t = data_binning.get("binning_t")
def binning_xb = data_binning.get("binning_xb")
def binning_q2 = data_binning.get("binning_q2")
def binning_scheme = [binning_xb,binning_q2,binning_t]


// *********************** Define Histograms **************************** //
file_loc_histograms = "../../histogram_dict.json"
def data_histograms = jsonSlurper.parse(new File(file_loc_histograms))
def histogram_array = []


for (hist in data_histograms){
    
    def hist_params = (hist.getValue()[0])

    def root_title= hist_params.get("root_title")
    def display_title = hist_params.get("display_title")

	def pre_fill = hist_params.get("pre_fill")
	def pro_fill = hist_params.get("pro_fill")
	def prex_fill = hist_params.get("prex_fill")
	def ex_fill = hist_params.get("ex_fill")      

    def num_bins_x = hist_params.get("num_bins_x")
    def x_bin_min = hist_params.get("x_bin_min")
    def x_bin_max = hist_params.get("x_bin_max")
	def fill_x = hist_params.get("fill_x")

    def num_bins_z = hist_params.get("num_bins_z")
    def z_bin_min = hist_params.get("z_bin_min")
    def z_bin_max = hist_params.get("z_bin_max")
	def fill_z = hist_params.get("fill_z")


	def bins_xb = hist_params.get("bins_xb")
	def bins_q2 = hist_params.get("bins_q2")
	def bins_t = hist_params.get("bins_t")

	def histo_couplet = []

	def hist_mini_array = []

	if (bins_xb == "yes"){
		if (bins_q2 =="yes"){
				def x1_1 	= [:].withDefault{new H1F(root_title + "_excut_cd_" + it,display_title + " Excl. Cuts, CD, " + it ,num_bins_x, x_bin_min, x_bin_max)}
				def x1_2 	= [:].withDefault{new H1F(root_title + "_excut_fd_" + it,display_title + " Excl. Cuts, FD, " + it,num_bins_x, x_bin_min, x_bin_max)}
				def x1_3 	= [:].withDefault{new H1F(root_title + "_excut_all_" + it,display_title + " Excl. Cuts, All, " + it,num_bins_x, x_bin_min, x_bin_max)}
				def x1_4 	= [:].withDefault{new H1F(root_title + "_prexcut_cd_" + it,display_title + " Pre-Excl. Cuts, CD, " + it ,num_bins_x, x_bin_min, x_bin_max)}
				def x1_5 	= [:].withDefault{new H1F(root_title + "_prexcut_fd_" + it,display_title + " Pre-Excl. Cuts,  FD, " + it,num_bins_x, x_bin_min, x_bin_max)}
				def x1_6 	= [:].withDefault{new H1F(root_title + "_prexcut_all_" + it,display_title + " Pre-Excl. Cuts,  All, " + it,num_bins_x, x_bin_min, x_bin_max)}

				hist_mini_array.add(x1_1)
				hist_mini_array.add(x1_2)
				hist_mini_array.add(x1_3)
				hist_mini_array.add(x1_4)
				hist_mini_array.add(x1_5)
				hist_mini_array.add(x1_6)

				histogram_array.add([hist_params, hist_mini_array])
		}
	}
	else{
		if (num_bins_z > 0){
			if (ex_fill == "yes"){
				def x2_1 = new H2F(root_title+"_excut_cd", display_title+" Excl. Cuts, CD", num_bins_x, x_bin_min, x_bin_max, num_bins_z, z_bin_min, z_bin_max)
				def x2_2 = new H2F(root_title+"_excut_fd", display_title+" Excl. Cuts, FD", num_bins_x, x_bin_min, x_bin_max, num_bins_z, z_bin_min, z_bin_max)
				def x2_3 = new H2F(root_title+"_excut_all", display_title+" Excl. Cuts, All", num_bins_x, x_bin_min, x_bin_max, num_bins_z, z_bin_min, z_bin_max)	

				hist_mini_array.add(x2_1)
				hist_mini_array.add(x2_2)
				hist_mini_array.add(x2_3)	
			}
			if (prex_fill == "yes"){
				def x2_4 = new H2F(root_title+"_prexcut_cd", display_title+" Pre-Excl. Cuts, CD", num_bins_x, x_bin_min, x_bin_max, num_bins_z, z_bin_min, z_bin_max)
				def x2_5 = new H2F(root_title+"_prexcut_fd", display_title+" Pre-Excl. Cuts, FD", num_bins_x, x_bin_min, x_bin_max, num_bins_z, z_bin_min, z_bin_max)
				def x2_6 = new H2F(root_title+"_prexcut_all", display_title+" Pre-Excl. Cuts, All", num_bins_x, x_bin_min, x_bin_max, num_bins_z, z_bin_min, z_bin_max)	

				hist_mini_array.add(x2_4)
				hist_mini_array.add(x2_5)
				hist_mini_array.add(x2_6)					
			}
			if (pro_fill == "yes"){
				def x2_7 = new H2F(root_title+"_nocut_cd", display_title+" No Cuts, CD", num_bins_x, x_bin_min, x_bin_max, num_bins_z, z_bin_min, z_bin_max)
				def x2_8 = new H2F(root_title+"_nocut_fd", display_title+" No Cuts, FD", num_bins_x, x_bin_min, x_bin_max, num_bins_z, z_bin_min, z_bin_max)
				def x2_9 = new H2F(root_title+"_nocut_all", display_title+" No Cuts, All", num_bins_x, x_bin_min, x_bin_max, num_bins_z, z_bin_min, z_bin_max)
					
				hist_mini_array.add(x2_7)
				hist_mini_array.add(x2_8)
				hist_mini_array.add(x2_9)
			}
				
			histo_couplet.add(hist_params)
			histo_couplet.add(hist_mini_array)
			histogram_array.add(histo_couplet)	
		}
		else {
			if (ex_fill == "yes"){
				def x1_1 = new H1F(root_title+"_excut_cd", display_title+" Excl. Cuts, CD", num_bins_x, x_bin_min, x_bin_max)
				def x1_2 = new H1F(root_title+"_excut_fd", display_title+" Excl. Cuts, FD", num_bins_x, x_bin_min, x_bin_max)
				def x1_3 = new H1F(root_title+"_excut_all", display_title+" Excl. Cuts, All", num_bins_x, x_bin_min, x_bin_max)
		
				hist_mini_array.add(x1_1)
				hist_mini_array.add(x1_2)
				hist_mini_array.add(x1_3)	
			}
			if (prex_fill == "yes"){
				def x1_4 = new H1F(root_title+"_prexcut_cd", display_title+" Pre-Excl. Cuts, CD", num_bins_x, x_bin_min, x_bin_max)
				def x1_5 = new H1F(root_title+"_prexcut_fd", display_title+" Pre-Excl. Cuts, FD", num_bins_x, x_bin_min, x_bin_max)
				def x1_6 = new H1F(root_title+"_prexcut_all", display_title+" Pre-Excl. Cuts, All", num_bins_x, x_bin_min, x_bin_max)

				hist_mini_array.add(x1_4)
				hist_mini_array.add(x1_5)
				hist_mini_array.add(x1_6)					
			}
			if (pro_fill == "yes"){
				def x1_7 = new H1F(root_title+"_nocut_cd", display_title+" No Cuts, CD", num_bins_x, x_bin_min, x_bin_max)
				def x1_8 = new H1F(root_title+"_nocut_fd", display_title+" No Cuts, FD", num_bins_x, x_bin_min, x_bin_max)
				def x1_9 = new H1F(root_title+"_nocut_all", display_title+" No Cuts, All", num_bins_x, x_bin_min, x_bin_max)
	
				hist_mini_array.add(x1_7)
				hist_mini_array.add(x1_8)
				hist_mini_array.add(x1_9)
			}
			if (pre_fill == "yes"){
				def x1_10 = new H1F(root_title, display_title, num_bins_x, x_bin_min, x_bin_max)

				hist_mini_array.add(x1_10)
			}
				
			histo_couplet.add(hist_params)
			histo_couplet.add(hist_mini_array)
			histogram_array.add(histo_couplet)	
	
		}		
	}
}



// *********************** Define Cuts **************************** //
json_cuts_file = "../../cut_dict.json"
def cuts_json = jsonSlurper.parse(new File(json_cuts_file))
def cuts_array = []
for (cut_type in cuts_json){
	cuts_array.add(cut_type.getValue()[0])
}


// ******************************************************************* //
// ****************** Running Through Files ************************** //
// ******************************************************************* //

//********************* Display pre reunning statistics **************** //
println("\n \nThe following files will be processed: ")
for (FileName in FilesToProcess){
	GlobalFileSizeToProcess+= FileName.length()
	println("$FileName - " + (FileName.length()/Mil/1000).round(2)+"GB")
}
println("\n" + (GlobalFileSizeToProcess/Mil/1000).round(2)+" GB is the total file size \n \n")



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
		def NumLocalFDDVEPEvents = 0
		def NumLocalCDDVEPEvents = 0
		def NumLocalFDAllEvents = 0
		def NumLocalCDAllEvents = 0
		def FileStartTime = new Date()
		println("\n \n \n \n Processing file $fname_short - ${(NumEventsToProcess/Mil).round(3)} M events events at ${FileStartTime.format('HH:mm:ss')}")


		
		// ******* Pass to event processor, increment variables of interest ****** //
		for (int j=0; j < NumEventsToProcess; j++) {
			evcount.getAndIncrement()
			su.UpdateScreen(FileStartTime.getTime(),evcount.get(),CountRate.toInteger(),NumEventsToProcess,fname_short)
			def event = reader.getNextEvent()
			funreturns = eventProcessor.processEvent(j,event,histogram_array,FCupCharge,cuts_array,binning_scheme)
			FCupCharge = funreturns[0]
			NumLocalDVPPEvents += funreturns[1]
			NumLocalFDDVEPEvents += funreturns[2]
			NumLocalCDDVEPEvents += funreturns[3]
			NumLocalFDAllEvents += funreturns[4]
			NumLocalCDAllEvents += funreturns[5]
			histogram_array = funreturns[6]
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
		println(" approximate global finish time at ${ReadableETA} ")

		// ******* Compile and print Physics statistics ****** //

		NumGlobalCDDVEPEvents += NumLocalCDDVEPEvents
		NumGlobalFDDVEPEvents += NumLocalFDDVEPEvents

		NumGlobalCDAllEvents += NumLocalCDAllEvents
		NumGlobalFDAllEvents += NumLocalFDAllEvents

		NumGlobalDVPPEvents += NumLocalDVPPEvents
		GlobalLumiTotal += lumicalc.CalcLumi(FCupCharge)

		println("Global DVPP Events Found: $NumGlobalDVPPEvents, out of $GlobalNumEventsProcessed")
		println("Global DVPP FD Events Found: $NumGlobalFDDVEPEvents, compared to $NumGlobalCDDVEPEvents DVPP events in the CD")
		println("Global FD Events (all) Found: $NumGlobalFDAllEvents, compared to $NumGlobalCDAllEvents events (all) in the CD")
		println("Charge on FCup from this run: $FCupCharge in nanoColoumbs")
		println("Total Integrated Luminosity so far is $GlobalLumiTotal-- UNITS???")

	}
}



// ******************************************************************* //
// ****************** File Saving and Cleanup************************* //
// ******************************************************************* //

//********* Output final running statistics *****************

def ScriptEndTime = new Date()
def ScriptRunTime = (ScriptEndTime.getTime() - ScriptStartTime.getTime())/1000/60 //Time to process file in minutes
def outputfilename = output_folder_groovy


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
println("File saved at ../../analysis_outputs/${output_folder_groovy}/${outputfilename}. \n")

//********* Save data in hipo and text files *****************

def phi_xbdir = "/PhiPlots"
def t_xbdir = "/t_Plots"


TDirectory out = new TDirectory()
out.mkdir('/'+OutFileName)
out.cd('/'+OutFileName)
out.mkdir('/'+OutFileName+phi_xbdir)
out.mkdir('/'+OutFileName+t_xbdir)


/*
def histos_json_out = histogram_array //Drops the last 3 elements in the array
histos_json_out.each { i ->
	out.addDataSet(i)
}
*/

for (histo_couplet in histogram_array){		
					//unpack
	hist_params = histo_couplet[0]
	hist_mini_array = histo_couplet[1]

	if ((hist_params.get("bins_xb") == "yes") && (hist_params.get("bins_q2") == "yes")){
		if (hist_params.get("bins_t") == "yes"){
			out.cd('/'+OutFileName+phi_xbdir)
			for (int hist_ind=0; hist_ind < hist_mini_array.size(); hist_ind++){

				def hist_object =  hist_mini_array[hist_ind]
				for (int xbi=0;xbi< binning_xb.size()-1;xbi++){
					for (int q2i=0;q2i< binning_q2.size()-1;q2i++){
						for (int ti=0;ti< binning_t.size()-1;ti++){

							def lowxb = (binning_xb[xbi]).toFloat().round(3)
							def highxb = (binning_xb[xbi+1]).toFloat().round(3)
							def lowq2 = (binning_q2[q2i]).toFloat().round(3)
							def highq2 = (binning_q2[q2i+1]).toFloat().round(3)
							def lowt = (binning_t[ti]).toFloat().round(3)
							def hight = (binning_t[ti+1]).toFloat().round(3)
							
							def title_xB = " $lowxb < xB < $highxb, "
							def title_q2 = "$lowq2 < q2 < $highq2, "
							def title_t = "$lowt < t < $hight"

							out.addDataSet(hist_object[title_xB+title_q2+title_t])
						}
					}
				}

			}

		}
		if (hist_params.get("bins_t") == "no"){	
			out.cd('/'+OutFileName+t_xbdir)
			

			for (int hist_ind=0; hist_ind < hist_mini_array.size(); hist_ind++){
				

				def hist_object =  hist_mini_array[hist_ind]
				for (int xbi=0;xbi< binning_xb.size()-1;xbi++){
					for (int q2i=0;q2i< binning_q2.size()-1;q2i++){

						def lowxb = (binning_xb[xbi]).toFloat().round(3)
						def highxb = (binning_xb[xbi+1]).toFloat().round(3)
						def lowq2 = (binning_q2[q2i]).toFloat().round(3)
						def highq2 = (binning_q2[q2i+1]).toFloat().round(3)

						def title_xB = " $lowxb < xB < $highxb, "
						def title_q2 = "$lowq2 < q2 < $highq2, "
						out.addDataSet(hist_object[title_xB+title_q2])
					
					}
				}

			}

		}
	}
	else{
		out.cd('/'+OutFileName)				
		for (hist_object in hist_mini_array){
			out.addDataSet(hist_object)
		}
	}
}




out.writeFile("../../analysis_outputs/${output_folder_groovy}/${outputfilename}.hipo")



File file = new File("../../analysis_outputs/${output_folder_groovy}/${outputfilename}.txt")
file.append("Run information for ${outputfilename}.hipo \n")
file.append("Run message: $output_message \n")
file.append("Script began at ${ScriptStartTime.format('MM/dd/YYYY-HH:mm:ss')} and finished at ${ScriptEndTime.format('MM/dd/YYYY-HH:mm:ss')}\n")
if(ScriptRunTime > 1){
	file.append("total runtime: ${ScriptRunTime.round(2)} minutes - ")
}
else{
	file.append("total runtime: ${(ScriptRunTime*60).round(2)} seconds - ")
}
file.append("Processing rate: ${(GlobalNumEventsProcessed/ScriptRunTime/60/1000).round(2)} kHz \n")
if (NumGlobalDVPPEvents>0){
	file.append("Final global number of DVPP events found: $NumGlobalDVPPEvents out of a total of $GlobalNumEventsProcessed - ${(NumGlobalDVPPEvents/GlobalNumEventsProcessed*100).round(2)} % of all events\n")
}
else{
	file.append("No DVPP events found out of a total of $GlobalNumEventsProcessed \n ")
}
if (NumGlobalCDDVEPEvents >0){
	file.append("Global FD DVPP Events Found: $NumGlobalFDDVEPEvents, compared to $NumGlobalCDDVEPEvents DVPP events in the CD, a ratio of ${(NumGlobalFDDVEPEvents/NumGlobalCDDVEPEvents*100).round(1)} %\n")
}
else{
	file.append("No DVEP events found in CD, all (if any) DVEP events are in FD \n")
}
if (NumGlobalCDAllEvents >0){
	file.append("Global FD Events (all) Found: $NumGlobalFDAllEvents, compared to $NumGlobalCDAllEvents in the CD, a ratio of ${(NumGlobalFDAllEvents/NumGlobalCDAllEvents*100).round(1)} %\n")
	file.append("The ratio of FD DVEP events to all events in FD is  ${(NumGlobalFDDVEPEvents/NumGlobalFDAllEvents*100).round(3)} %\n")
	file.append("The ratio of CD DVEP events to all events in CD is  ${(NumGlobalCDDVEPEvents/NumGlobalCDAllEvents*100).round(3)} %\n")
	file.append("The number of (all) events not in CD or FD is  ${(GlobalNumEventsProcessed-NumGlobalCDAllEvents-NumGlobalFDAllEvents)} %\n")
}


//double d = 3.7578845854848E41;
//double d2 = 3.416436417734133;
DecimalFormat f = new DecimalFormat("0.###E0");
//System.out.println();

file.append("Total Integrated Luminosity from the runs processed is ${(f.format(GlobalLumiTotal))} \n")
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

file.append("\n \n \n ***************** \n Used the follwowing binning scheme \n")
for (key in data_binning.keySet()){
	file.append("$key was ${data_binning.get(key)} \n")
}


file.append("\n \n \n ***************** \n Used ${NumCores} cores to try to process ${NumFilesToProcess} files, processing a total of ${NumFilesProcessed} files. The following files were processed: \n")
for (filename in FilesToProcess){
	file.append("${filename} \n")
}

