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

total_counts = 0
lumi_total = 0
def GlobalNumEventsProcessed = 0
def GlobalRunTime = 0
def num_ep_events = 0
def NumGlobalDVPPEvents = 0
def NumFilesProcessed = 0
def GlobalFileSizeToProcess = 0
def GlobalFileSizeProcessed = 0

//********************* Define Histograms **************** //
def hxB = new H1F("Hist_xB","Bjorken x Distribution",1000,0,1.5)





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

		def NumEventsInFile= reader.getSize().toInteger()
		def NumEventsToProcess = DesiredNumEventsToProcess
			if (DesiredNumEventsToProcess > NumEventsInFile){NumEventsToProcess = NumEventsInFile}
			if (DesiredNumEventsToProcess == 0){NumEventsToProcess = NumEventsInFile}
		def CountRate = NumEventsToProcess/2
			if (CountRate > 100000){CountRate = 100000} //If processing large number of events, update screen ever X events, where X is arbitrarily selected

		

		// *********** Define file specific stats *********
		def date = new Date() // for runtime statistics
		def evcount = new AtomicInteger() // for counting how many events have been processed
		evcount.set(0)
		def Float fcc_final = 0 //For counting charge on faraday cup
		def NumLocalDVPPEvents = 0
	
		def FileStartTime = date.getTime()
		printerUtil.printer("Processing file $fname - ${(NumEventsToProcess/Mil).round(3)} M events events at ${date.format('HH:mm:ss')}",1)


		
		// ******* Pass to event processor, increment variables of interest ****** //
		for (int j=0; j < NumEventsToProcess; j++) {
			evcount.getAndIncrement()
			su.UpdateScreen(FileStartTime,evcount.get(),CountRate.toInteger(),NumEventsToProcess,fname)
			def event = reader.getNextEvent()
			funreturns = eventProcessor.processEvent(event,hxB,fcc_final)
			fcc_final = funreturns[0]
			NumLocalDVPPEvents += funreturns[1]
			hxB = funreturns[2]
		}

		reader.close()

		// ******* Compile and print runtime statistics ****** //

		FileEndTime = new Date()
		def TotalFileRunTime = (FileEndTime.getTime() - FileStartTime)/1000/60 //Time to process file in minutes
		NumFilesProcessed++
		GlobalRunTime += TotalFileRunTime
		GlobalNumEventsProcessed += NumEventsToProcess
		GlobalFileSizeProcessed += fname.length() //This is a measure of how many bits have already been processed
		def TotalTimeLeft = GlobalRunTime*(GlobalFileSizeToProcess-GlobalFileSizeProcessed)/GlobalFileSizeProcessed //seconds per bit*bits left to process 
		def unixtimeETA = Math.round(TotalTimeLeft*60+FileEndTime.getTime()/1000) //add time left to current time
		def ReadableETA = Date.from(Instant.ofEpochSecond(unixtimeETA)).format('HH:mm:ss') //convert unix timestamp to readable time
		

		print("Finished processing ${(NumEventsToProcess/Mil).round(2)} M events at ${date.format('HH:mm:ss')}, ")
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


		println("Global DVPP Events Found: $NumGlobalDVPPEvents, out of $GlobalNumEventsProcessed")

		//println("final fcup charge in nc: "+fcc_final)
		//println(lumicalc.CalcLumi(fcc_final))
		//lumi_total = lumi_total + lumicalc.CalcLumi(fcc_final)
		//println("total lumi is " + lumi_total)

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
println("Final global number of DVPP events found: $NumGlobalDVPPEvents out of a total of $GlobalNumEventsProcessed")

//println("final total lumi is " + lumi_total)

//********* Save data in hipo file *****************
TDirectory out = new TDirectory()
out.mkdir('/'+OutFileName)
out.cd('/'+OutFileName)
out.addDataSet(hxB)
out.writeFile("${OutFileName}${ScriptEndTime.format('HH:mm')}.hipo")