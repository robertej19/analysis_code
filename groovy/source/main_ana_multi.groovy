#!/usr/bin/groovy

import java.io.*
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.Date
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
import groovy.io.FileType
import groovyx.gpars.GParsPool
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

MyMods.enable() //I don't know what this does, its from Andrey, don't touch it, it works

println("\n \n \n \n \n \n \n \n \n \n \n \n \n \n")

Mil = 1000000

dvmp_counts = 0


def printer(string,override){
	k = 0
	if(k==1){
		println("\n"+string+"\n")
		if(override==2){
			println(string)
		}
	}
	if(k==0){
		if(override==1){
			println(string+"\n")
		}
		if(override==2){
			println(string)
		}
	}
}

def FileGetter(FileLocation){
	def FileList = []
	def dir = new File(FileLocation)
	dir.eachFileRecurse (FileType.FILES) { file ->
		FileList << file
	}
	return FileList
}

def processEvent(event,hxB) {
	def beam = LorentzVector.withPID(11,0,0,10.6)
	def target = LorentzVector.withPID(2212,0,0,0)

	def banknames = ['REC::Event','REC::Particle','REC::Cherenkov','REC::Calorimeter','REC::Traj','REC::Track','REC::Scintillator']

	if(banknames.every{event.hasBank(it)}) {
		def (evb,partb,cc,ec,traj,trck,scib) = banknames.collect{event.getBank(it)}
		def banks = [cc:cc,ec:ec,part:partb,traj:traj,trck:trck]
		def ihel = evb.getByte('helicity',0)
		printer("ihel is "+ihel,0)


		def index_of_electrons_and_protons = (0..<partb.rows()).findAll{partb.getInt('pid',it)==11 && partb.getShort('status',it)<0}
			.collectMany{iele->(0..<partb.rows()).findAll{partb.getInt('pid',it)==2212}.collect{ipro->[iele,ipro]}
		}
		printer("index_of_electrons_and_protons "+index_of_electrons_and_protons,0)


		def isep0s = index_of_electrons_and_protons.findAll{iele,ipro->
			def ele = LorentzVector.withPID(11,*['px','py','pz'].collect{partb.getFloat(it,iele)})
			def pro = LorentzVector.withPID(2212,*['px','py','pz'].collect{partb.getFloat(it,ipro)})
			printer("first electron is"+ele,0)



		

			def qvec = beam-ele



			def xBjorken = -qvec.mass2()/(2*pro.vect().dot(qvec.vect()))
			printer("adding XB to hist "+index_of_electrons_and_protons,0)

			hxB.fill(xBjorken)

			
		}
	}
}

def screen_updater(FileStartTime,CurrentCounter,CountRate,NumTotalCounts,filename){
	if(CurrentCounter % CountRate == 0){
		runtime = new Date()
		TimeElapsed = (runtime.getTime() - FileStartTime)/1000/60
		CountsLeft = NumTotalCounts-CurrentCounter
		TimeLeft = TimeElapsed/CurrentCounter*CountsLeft
		Rate = CurrentCounter/TimeElapsed/1000/60
		uTS = Math.round(TimeLeft*60+runtime.getTime()/1000)
		eta = Date.from(Instant.ofEpochSecond(uTS)).format('HH:mm:ss')

		//printer("Total running time in minutes is: ${TimeElapsed.round(2)}",2)
		printer("${(CurrentCounter/Mil).round(2)}M events have been processed from ${filename}, ${(CountsLeft/Mil).round(2)}M events remain",2)
		printer("Processing Rate is ${Rate.round(1)} kHz",2)
		printer("Anticipated file finish time is $eta, ${TimeLeft.round(2)} minutes left",1)
		return TimeLeft
	}
}


def hxB = new H1F("Hist_xB","Bjorken x Distribution",1000,0,1.5)

if (args.size()<3) {
	printer("You need to include the number of events and files you want to process in the start command!",1)
	printer("For example, <run-groovy filename.groovy hipo_file_to_process.hipo 1000 10",1)
}

def NumFilesToProcess = args[2].toInteger()
def FilesToProcess = FileGetter(args[0]).take(NumFilesToProcess)
def DesiredNumEventsToProcess = args[1].toInteger()
printer("The following files have been found: ",1)
def total_file_length = 0
def file_length_processed = 0
for (FileName in FilesToProcess){
	total_file_length += FileName.length()
	printer("$FileName",2)
}

printer((total_file_length/Mil/1000).round(2)+"G is the total file size in Bites (?)",2)

def TotalNumEventsProcessed = 0
def TotalRunTime = 0
def num_ep_events = 0
def num_dvpp_events = 0


//def exe = Executors.newScheduledThreadPool(1)
//exe.scheduleWithFixedDelay(save, 5, 30, TimeUnit.SECONDS)

//GParsPool.withPool 4, {
//  args.eachParallel{fname->
//  }
//q}


starttime = new Date()
def procStartTime = starttime.getTime()





GParsPool.withPool 6, {
	
		//for (int i=0; i < FilesToProcess.size(); i++) {
			//print(args.length)
			//args.eachParallel{fname->
			FilesToProcess.eachParallel{fname->
				def reader = new HipoDataSource()
				//def fname = FilesToProcess[i]
				reader.open(fname)
				printer("processing file ${fname}",1)
				def NumEventsInFile= reader.getSize().toInteger()
				def NumEventsToProcess = DesiredNumEventsToProcess
				if (DesiredNumEventsToProcess == 0){NumEventsToProcess = NumEventsInFile}
				//printer("\n \n $fname has ${(NumEventsToProcess/Mil).round(2)} M events, is file number ${i+1} of ${FilesToProcess.size()}",2)
				def evcount = new AtomicInteger()
				evcount.set(0)

				def date = new Date()
				def FileStartTime = date.getTime()
				printer("Process file $fname at ${date.format('HH:mm:ss')}",1)


				def CountRate = NumEventsToProcess/10
				for (int j=0; j < NumEventsToProcess; j++) {
					evcount.getAndIncrement()
					screen_updater(FileStartTime,evcount.get(),CountRate.toInteger(),NumEventsToProcess,fname)
					def event = reader.getNextEvent()
					processEvent(event,hxB)
				}

				endtime = new Date()
				def TotalFileRunTime = (endtime.getTime() - FileStartTime)/1000/60
				printer("Finished processing ${(NumEventsToProcess/Mil).round(2)} M events at ${date.format('HH:mm:ss')},total run time ${TotalFileRunTime.round(2)} minutes",1)
				reader.close()

				TotalRunTime += TotalFileRunTime
				TotalNumEventsProcessed += NumEventsToProcess
				//printer("Processed ${(i+1)} files, ${(TotalNumEventsProcessed/Mil).round(2)} M events, have ${FilesToProcess.size()-i-1} files left to process",1)

				//def TotalTimeLeft = TotalRunTime*(FilesToProcess.size()-i-1)/(i+1)
				//file_length_processed += fname.length()
				//def TotalTimeLeft = TotalRunTime*(total_file_length-file_length_processed)/file_length_processed
				//uTSX = Math.round(TotalTimeLeft*60+endtime.getTime()/1000)
				//def etaTotal = Date.from(Instant.ofEpochSecond(uTSX)).format('HH:mm:ss')
				//p//rinter("Total Run Time of ${TotalRunTime.round(2)} minutes, approximate finish time at ${etaTotal} ",1)
				//printer("Total num DVMP counts so far is $dvmp_counts",2)
				reader.close()

			}
	}
//}


def dateX = new Date()
printer("Finished processing all files at ${dateX.format('HH:mm:ss')}",1)
def OutFileName = "output_file_histos"
TDirectory out = new TDirectory()
out.mkdir('/'+OutFileName)
out.cd('/'+OutFileName)
out.addDataSet(hxB)
out.writeFile(" ${OutFileName} ${dateX.format('HH:mm:ss')}.hipo")

//printer("confirm start time of ${procStartTime.format('HH:mm:ss')}",1)

//exe.shutdown()