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


MyMods.enable() //I don't know what this does, its from Andrey, don't touch it, it works

println("\n \n \n \n \n \n \n \n \n \n \n \n \n \n")

Mil = 1000000

dvmp_counts = 0

def eventProcessor = new EventProcessor()
def fg = new FileGetter()
printerUtil = new Printer() //This needs to be defined as global (no def before the object name) so that it can be used in all methods

def screen_updater(FileStartTime,CurrentCounter,CountRate,NumTotalCounts,filename){
	if(CurrentCounter % CountRate == 0){
		runtime = new Date()
		TimeElapsed = (runtime.getTime() - FileStartTime)/1000/60
		CountsLeft = NumTotalCounts-CurrentCounter
		TimeLeft = TimeElapsed/CurrentCounter*CountsLeft
		Rate = CurrentCounter/TimeElapsed/1000/60
		uTS = Math.round(TimeLeft*60+runtime.getTime()/1000)
		eta = Date.from(Instant.ofEpochSecond(uTS)).format('HH:mm:ss')

		//printerUtil.printer("Total running time in minutes is: ${TimeElapsed.round(2)}",2)
		printerUtil.printer("${(CurrentCounter/Mil).round(2)}M events have been processed from ${filename}, ${(CountsLeft/Mil).round(2)}M events remain",2)
		printerUtil.printer("Processing Rate is ${Rate.round(1)} kHz",2)
		printerUtil.printer("Anticipated file finish time is $eta, ${TimeLeft.round(2)} minutes left",1)
		return TimeLeft
	}
}



def hxB = new H1F("Hist_xB","Bjorken x Distribution",1000,0,1.5)

if (args.size()<3) {
	printerUtil.printer("You need to include the number of events and files you want to process in the start command!",1)
	printerUtil.printer("For example, <run-groovy filename.groovy hipo_file_to_process.hipo 1000 10",1)
}



def NumFilesToProcess = args[2].toInteger()

def FilesToProcess = fg.GetFile(args[0]).take(NumFilesToProcess)

def DesiredNumEventsToProcess = args[1].toInteger()
printerUtil.printer("The following files have been found: ",1)
def total_file_length = 0
def file_length_processed = 0
for (FileName in FilesToProcess){
	total_file_length += FileName.length()
	printerUtil.printer("$FileName",2)
}



printerUtil.printer((total_file_length/Mil/1000).round(2)+"G is the total file size in Bites (?)",2)

def TotalNumEventsProcessed = 0
def TotalRunTime = 0
def num_ep_events = 0
def num_dvpp_events = 0

total_counts = 0
lumi_total = 0

starttime = new Date()
def procStartTime = starttime.getTime()


def CalcLumi(qFcup) {
	N_A = 6.02214E23 //avogadro's number
	l = 5 //length of target, in centimeters
	rho = 0.07 //density of liquid hydrogen target, in g/cm^3
	e = 1.602E-19 //charge of an electron, in Columbs

	Q = qFcup*1E-9 //charge is in units of nC, need to convert to Coulombs

	L = N_A*l*rho*Q/e

	return L //The luminosity that is returned has units of cm^-2

}




GParsPool.withPool 6, {
	FilesToProcess.eachParallel{fname->
		def reader = new HipoDataSource()
		def Float fcc_final = 0
		//def fname = FilesToProcess[i]
		reader.open(fname)
		printerUtil.printer("processing file ${fname}",1)
		def NumEventsInFile= reader.getSize().toInteger()
		
		dvmp_counts = 0
		def NumEventsToProcess = DesiredNumEventsToProcess
		if (DesiredNumEventsToProcess == 0){NumEventsToProcess = NumEventsInFile}
		//printerUtil.printer("\n \n $fname has ${(NumEventsToProcess/Mil).round(2)} M events, is file number ${i+1} of ${FilesToProcess.size()}",2)
		def evcount = new AtomicInteger()
		evcount.set(0)

		def date = new Date()
		def FileStartTime = date.getTime()
		printerUtil.printer("Process file $fname at ${date.format('HH:mm:ss')}",1)


		def CountRate = NumEventsToProcess/10
		for (int j=0; j < NumEventsToProcess; j++) {
			evcount.getAndIncrement()
			screen_updater(FileStartTime,evcount.get(),CountRate.toInteger(),NumEventsToProcess,fname)
			def event = reader.getNextEvent()
			funreturns = eventProcessor.processEvent(event,hxB,fcc_final)
			fcc_final = funreturns[0]
			hxB = funreturns[1]
			println(dvmp_counts)
		}


		println("processing file ${fname}")
		println("final fcup charge in nc: "+fcc_final)
		println(CalcLumi(fcc_final))

		total_counts = total_counts + dvmp_counts
		println("total caounts of dvmp is " + total_counts)
		lumi_total = lumi_total + CalcLumi(fcc_final)
		println("total lumi is " + lumi_total)

		endtime = new Date()
		def TotalFileRunTime = (endtime.getTime() - FileStartTime)/1000/60
		printerUtil.printer("Finished processing ${(NumEventsToProcess/Mil).round(2)} M events at ${date.format('HH:mm:ss')},total run time ${TotalFileRunTime.round(2)} minutes",1)
		reader.close()

		TotalRunTime += TotalFileRunTime
		TotalNumEventsProcessed += NumEventsToProcess
		//printerUtil.printer("Processed ${(i+1)} files, ${(TotalNumEventsProcessed/Mil).round(2)} M events, have ${FilesToProcess.size()-i-1} files left to process",1)

		//def TotalTimeLeft = TotalRunTime*(FilesToProcess.size()-i-1)/(i+1)
		//file_length_processed += fname.length()
		//def TotalTimeLeft = TotalRunTime*(total_file_length-file_length_processed)/file_length_processed
		//uTSX = Math.round(TotalTimeLeft*60+endtime.getTime()/1000)
		//def etaTotal = Date.from(Instant.ofEpochSecond(uTSX)).format('HH:mm:ss')
		//p//rinter("Total Run Time of ${TotalRunTime.round(2)} minutes, approximate finish time at ${etaTotal} ",1)
		//printerUtil.printer("Total num DVMP counts so far is $dvmp_counts",2)
		reader.close()

	}
}


println("final total caounts of dvmp is " + total_counts)
println("final total lumi is " + lumi_total)

def dateX = new Date()
printerUtil.printer("Finished processing all files at ${dateX.format('HH:mm:ss')}",1)
def OutFileName = "output_file_histos"
TDirectory out = new TDirectory()
out.mkdir('/'+OutFileName)
out.cd('/'+OutFileName)
out.addDataSet(hxB)
out.writeFile("${OutFileName}${dateX.format('HH:mm:ss')}.hipo")

//printerUtil.printer("confirm start time of ${procStartTime.format('HH:mm:ss')}",1)

//exe.shutdown()