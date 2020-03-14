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

MyMods.enable() //I don't know what this does, its from Andrey, don't touch it, it works

println("\n \n \n \n \n \n \n \n \n \n \n \n \n \n")

Mil = 1000000

dvmp_counts = 0

/////////////////

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

def processEvent(event,hhel,hphi,hq2,hW,hxB,H_xB_Q2,heleTheta,hproTheta,heleproTheta,heleproThetaDVPP,htmom,htmomrecon,hLeptHadAngle,hproThetaFD,hproThetaCD,Hist_beta_p,Hist_beta_T,Hist_Ultra_Phi,t_bins) {
	def beam = LorentzVector.withPID(11,0,0,10.6)
	def target = LorentzVector.withPID(2212,0,0,0)

	def banknames = ['REC::Event','REC::Particle','REC::Cherenkov','REC::Calorimeter','REC::Traj','REC::Track','REC::Scintillator']

	if(banknames.every{event.hasBank(it)}) {
		def (evb,partb,cc,ec,traj,trck,scib) = banknames.collect{event.getBank(it)}
		def banks = [cc:cc,ec:ec,part:partb,traj:traj,trck:trck]
		def ihel = evb.getByte('helicity',0)
		printer("ihel is "+ihel,0)

		//println partb.getInt('status')



/*
if bankname[scint][particle_index] == 4 OR particle.status > 4000
	proton in CD
if bankname[scint][particle_index] == 12 OR particle.status > 4000
	proton in FD

		def particle_stati = recon_Particles.getInt('status')

		def scint_sectors = [recon_Scint.getShort('pindex')*.toInteger(), recon_Scint.getInt('sector')].transpose().collectEntries()
		def scint_detectors = [recon_Scint.getShort('pindex')*.toInteger(), recon_Scint.getInt('detector')].transpose().collectEntries()

		def recon_Particles = event.getBank("REC::Particle")
		def recon_Cal = event.getBank("REC::Calorimeter")
		def recon_Scint = event.getBank("REC::Scintillator")

		if(scint_detectors[particle_index]==4){
			printer("particle detected in CTOF",0)

			1000×FT + 2000×FD + 4000×CD
where FT/FD/CD are 1 if that detector subsystem contributed to the particle, else 0

Going forward I would encourage always separating any distribution into those two regions, especially when looking at particle kinematics.
I would suggest that in the title where you have "Electron" and "Proton" to add either FD or FTOF, CD or CTOF. to help clarify this.
When it comes to presenting, this will be the first question.
*/




		def index_of_electrons_and_protons = (0..<partb.rows()).findAll{partb.getInt('pid',it)==11 && partb.getShort('status',it)<0}
			.collectMany{iele->(0..<partb.rows()).findAll{partb.getInt('pid',it)==2212}.collect{ipro->[iele,ipro]}
		}
		printer("index_of_electrons_and_protons "+index_of_electrons_and_protons,0)

		def index_of_pions = (0..<partb.rows()-1).findAll{partb.getInt('pid',it)==22 && partb.getShort('status',it)>=2000}
			.findAll{ig1->'xyz'.collect{partb.getFloat("p$it",ig1)**2}.sum()>0.16}
			.collectMany{ig1->
			(ig1+1..<partb.rows()).findAll{partb.getInt('pid',it)==22 && partb.getShort('status',it)>=2000}
			.findAll{ig2->'xyz'.collect{partb.getFloat("p$it",ig2)**2}.sum()>0.16}
			.collect{ig2->[ig1,ig2]}
		}
		printer("index of pions is " + index_of_pions,0)

		def isep0s = index_of_electrons_and_protons.findAll{iele,ipro->
			def ele = LorentzVector.withPID(11,*['px','py','pz'].collect{partb.getFloat(it,iele)})
			def pro = LorentzVector.withPID(2212,*['px','py','pz'].collect{partb.getFloat(it,ipro)})
			printer("first electron is"+ele,0)

			if(event.hasBank("MC::Particle")) {
				printer("Event has MC Particle bank!",0)
				def mcb = event.getBank("MC::Particle")
				def mfac = (partb.getShort('status',ipro)/1000).toInteger()==2 ? 3.2 : 2.5

				def profac = 0.9

				//mfac=1
				profac = 1.0

				ele = LorentzVector.withPID(11,*['px','py','pz'].collect{mcb.getFloat(it,0) + (partb.getFloat(it,iele)-mcb.getFloat(it,0))*mfac})
				pro = LorentzVector.withPID(2212,*['px','py','pz'].collect{profac*(mcb.getFloat(it,1) + (partb.getFloat(it,ipro)-mcb.getFloat(it,1))*mfac)})
				printer("second electron is"+ele)
				def evec = new Vector3()
				evec.setMagThetaPhi(ele.p(), ele.theta(), ele.phi())
				def pvec = new Vector3()
				pvec.setMagThetaPhi(pro.p(), pro.theta(), pro.phi())
			}

			def eletheta = Math.toDegrees(ele.theta())
			def protheta = Math.toDegrees(pro.theta())
			if(protheta<0) protheta+=360

			heleTheta.fill(eletheta)
			hproTheta.fill(protheta)
			heleproTheta.fill(protheta,eletheta)

			if (partb.getInt('status',ipro) > 4000){
				hproThetaCD.fill(protheta)
			}
			if ((partb.getInt('status',ipro) < 4000) && (partb.getInt('status',ipro) > 2000)){
				hproThetaFD.fill(protheta)
			}

			def wvec = beam+target-ele
			def qvec = beam-ele
			def epx = beam+target-ele-pro
			//def t_sqrt = pro - target //t = (p'-p)^2
			//def t_MomTran = t_sqrt.vect().dot(t_sqrt.vect())

			printer("epx mass squared is:${epx.mass2()}",0)
			def xBjorken = -qvec.mass2()/(2*pro.vect().dot(qvec.vect()))
			printer("xB is " + xBjorken,0)

			def pdet = (partb.getShort('status',ipro)/1000).toInteger()==2 ? 'FD':'CD'

			def profi = Math.toDegrees(pro.phi())
			if(profi<0) profi+=360

			def esec = (0..<scib.rows()).find{scib.getShort('pindex',it)==iele}?.with{scib.getByte('sector',it)}
			def psec = (0..<scib.rows()).find{scib.getShort('pindex',it)==ipro}?.with{scib.getByte('sector',it)}
			if(psec==0) {
				psec = Math.floor(profi/60).toInteger() +2
				if(psec==7) psec=1
			}

			def isep0 = epx.mass2()<1 && wvec.mass()>2

			def pi0s = index_of_pions.collect{ig1,ig2->
				def g1 = LorentzVector.withPID(22,*['px','py','pz'].collect{partb.getFloat(it,ig1)})
				def g2 = LorentzVector.withPID(22,*['px','py','pz'].collect{partb.getFloat(it,ig2)})
				if(ele.vect().theta(g1.vect())>8 && ele.vect().theta(g2.vect())>8) {
					def gg = g1+g2
					def ggmass = gg.mass()
					def ispi0 = ggmass<0.2 && ggmass>0.07// && gg.p()>1.5

					if(ispi0) {
						def epggx = epx-gg
						def thetaXPi = epx.vect().theta(gg.vect())
						def dpt0 = epggx.px().abs()<0.3 && epggx.py().abs()<0.3
						def dmisse0 = epggx.e()<1
						def tt0 = -(pro-target).mass2()
						def procalc = beam+target-ele-gg
						def tt1 = -(procalc-target).mass2()


						def vLept = beam.vect().cross(ele.vect())
						def vHad = pro.vect().cross(gg.vect())
						def PlaneDot = vLept.dot(vHad)
						def cosangle = PlaneDot/vLept.mag()/vHad.mag()
						def LeptHadAngle = Math.toDegrees( Math.acos(cosangle))
						if (pro.vect().dot(vLept)<0){
							LeptHadAngle = -LeptHadAngle+360
						}

						def xb_bins = 10
						def q2Round = Math.round((-qvec.mass2())*2+0.5)/2
						def xBRound = Math.round(xb_bins*xBjorken+0.5)
						printer("Q2 is ${-qvec.mass2()} = $q2Round and xB is $xBjorken = $xBRound",0)

						if (q2Round < 0.4){
							printer("Q2 is low at $q2round",2)
						}

						def title = "${((xBRound-1)/xb_bins).round(2)} < xB < ${((xBRound)/xb_bins).round(2)}_ ${q2Round - 0.5} < q2 < ${q2Round+0.0}"
						//printer("Associated title is $title",2)
						Hist_beta_p[title].fill(xBjorken,-qvec.mass2())

						//println(t_bins)
						def tRound = (Math.round(tt0*10)/10)
						def UltraTitle = "88"

						def TitleUltra = 0

						if(isep0){
							if(ispi0 && isep0 && dmisse0 && dpt0 && thetaXPi<2){

								for(int xti=0;xti<=t_bins.size();xti++){
									if(t_bins[xti]<tt0 && tt0<t_bins[xti+1]){
										def low = (t_bins[xti]).toFloat().round(2)
										def high = (t_bins[xti+1]).toFloat().round(2)
										def TitleUltra = title +" " + "$low < t <  $high"
										println(TitleUltra)
									}
								}

								if(TitleUltra==0){
									println("Ultra is 0, t is $tt0")
								}


								//num_dvpp_events = num_dvpp_events + 1
								hhel.fill(ihel)
								hphi.fill(profi)
								hq2.fill(-qvec.mass2())
								htmom.fill(tt0)
								htmomrecon.fill(tt1)
								hW.fill(wvec.mass())
								hxB.fill(xBjorken)
								H_xB_Q2.fill(xBjorken,-qvec.mass2())
								heleproThetaDVPP.fill(protheta,eletheta)
								hLeptHadAngle.fill(LeptHadAngle)
								Hist_beta_T[title].fill(tt0)
								Hist_Ultra_Phi[TitleUltra].fill(LeptHadAngle)
								//printer("title of ispis is : $title",0)
								println(Math.round(tt0*10)/10)
								dvmp_counts = dvmp_counts +1
								//println("num dvmp counts is $dvmp_counts")
							}
						}
					}
					return ispi0
				}
			}
			return false
		}
	}
}

def screen_updater(FileStartTime,CurrentCounter,CountRate,NumTotalCounts){
	if(CurrentCounter % CountRate == 0){
		runtime = new Date()
		TimeElapsed = (runtime.getTime() - FileStartTime)/1000/60
		CountsLeft = NumTotalCounts-CurrentCounter
		TimeLeft = TimeElapsed/CurrentCounter*CountsLeft
		Rate = CurrentCounter/TimeElapsed/1000/60
		uTS = Math.round(TimeLeft*60+runtime.getTime()/1000)
		eta = Date.from(Instant.ofEpochSecond(uTS)).format('HH:mm:ss')

		//printer("Total running time in minutes is: ${TimeElapsed.round(2)}",2)
		printer("${(CurrentCounter/Mil).round(2)}M events have been processed, ${(CountsLeft/Mil).round(2)}M events remain",2)
		printer("Processing Rate is ${Rate.round(1)} kHz",2)
		printer("Anticipated file finish time is $eta, ${TimeLeft.round(2)} minutes left",1)
		return TimeLeft
	}
}

def Hist_beta_p 	= [:].withDefault{new H2F("Hist_beta_p${it}"		, "Beta vs. Momentum ${it}"		          ,100,0,1.5,100,0,12)}
def Hist_beta_T 	= [:].withDefault{new H1F("Hist_beta_T${it}", "T in q2 xb bins of ${it}",50,0,5)}
def Hist_Ultra_Phi 	= [:].withDefault{new H1F("Hist_Ultra_Phi${it}", "Phi in in q2 xb t bins of ${it}",20,0,360)}
def hhel = new H1F("Hist_ihel","helicity",7,-2,2)
def hphi = new H1F("Hist_phi","Phi Distribution",2500,-10,370)
def hq2 = new H1F("Hist_q2","Q^2 Distribution",1000,0,12)
def hW = new H1F("Hist_W","W Distribution",1000,0,12)
def hxB = new H1F("Hist_xB","Bjorken x Distribution",1000,0,1.5)
def htmom = new H1F("Hist_t_mom","Momentum transfer to Nucleon (t)",2000,0,5)
def htmomrecon = new H1F("Hist_t_mom_recon","Recon'd Momentum transfer to Nucleon (t)",4000,-20,20)
def heleTheta = new H1F("Hist_heleTheta","Electron Theta Distribution",2500,0,50)
def hproTheta = new H1F("Hist_hproTheta","Proton Theta Distribution",2500,0,150)
def hproThetaFD = new H1F("Hist_hproThetaFD","Proton Theta Distribution in FD",2500,0,150)
def hproThetaCD = new H1F("Hist_hproThetaCD","Proton Theta Distribution in CD",2500,0,150)
def heleproTheta = new H2F("Hist_heleproTheta","Proton Angle vs. Electron Angle (Theta)",800,0,150,800,0,55)
def heleproThetaDVPP = new H2F("Hist_heleproThetaDVMP","Proton Angle vs. Electron Angle (Theta) for DVPP Candidates",400,0,150,400,0,55)
def H_xB_Q2 = new H2F("Hist_xB_Q2" , "Bjorken X vs. Q^2",300,0,1.5,300,0,12)
def hLeptHadAngle = new H1F("Hist_LeptHadAngle" , "Angle Between Lepton and Hadron Planes",90,0,360)

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

printer(total_file_length+"This i sthe lenght",2)

def TotalNumEventsProcessed = 0
def TotalRunTime = 0
def num_ep_events = 0
def num_dvpp_events = 0

def t_bins = [0,0.6,1,1.5,2,3,5,8]

println(t_bins[0])

for (int i=0; i < FilesToProcess.size(); i++) {
	def reader = new HipoDataSource()
	def fname = FilesToProcess[i]
	reader.open(fname)
	def NumEventsInFile= reader.getSize().toInteger()
	def NumEventsToProcess = DesiredNumEventsToProcess
	if (DesiredNumEventsToProcess == 0){NumEventsToProcess = NumEventsInFile}
	printer("\n \n $fname has ${(NumEventsToProcess/Mil).round(2)} M events, is file number ${i+1} of ${FilesToProcess.size()}",2)
	def evcount = new AtomicInteger()
	evcount.set(0)

	def date = new Date()
	def FileStartTime = date.getTime()
	printer("Process file $fname at ${date.format('HH:mm:ss')}",1)


	def CountRate = NumEventsToProcess/10
	for (int j=0; j < NumEventsToProcess; j++) {
		evcount.getAndIncrement()
		screen_updater(FileStartTime,evcount.get(),CountRate.toInteger(),NumEventsToProcess)
		def event = reader.getNextEvent()
		processEvent(event,hhel,hphi,hq2,hW,hxB,H_xB_Q2,heleTheta,hproTheta,heleproTheta,heleproThetaDVPP,htmom,htmomrecon,hLeptHadAngle,hproThetaFD,hproThetaCD,Hist_beta_p,Hist_beta_T,Hist_Ultra_Phi,t_bins)
		//println "num ep events = " + num_ep_events
		//println "num dvpp evnets = " + num_dvpp_events
	}

	endtime = new Date()
	def TotalFileRunTime = (endtime.getTime() - FileStartTime)/1000/60
	printer("Finished processing ${(NumEventsToProcess/Mil).round(2)} M events at ${date.format('HH:mm:ss')},total run time ${TotalFileRunTime.round(2)} minutes",1)
	reader.close()

	TotalRunTime += TotalFileRunTime
	TotalNumEventsProcessed += NumEventsToProcess
	printer("Processed ${(i+1)} files, ${(TotalNumEventsProcessed/Mil).round(2)} M events, have ${FilesToProcess.size()-i-1} files left to process",1)

	//def TotalTimeLeft = TotalRunTime*(FilesToProcess.size()-i-1)/(i+1)
	file_length_processed += fname.length()
	def TotalTimeLeft = TotalRunTime*(total_file_length-file_length_processed)/file_length_processed
	uTSX = Math.round(TotalTimeLeft*60+endtime.getTime()/1000)
	def etaTotal = Date.from(Instant.ofEpochSecond(uTSX)).format('HH:mm:ss')
	printer("Total Run Time of ${TotalRunTime.round(2)} minutes, approximate finish time at ${etaTotal} ",1)
}

def dateX = new Date()
printer("Finished processing all files at ${dateX.format('HH:mm:ss')}",1)
printer("Total num DVMP counts is $dvmp_counts",2)
def OutFileName = "output_file_histos"
TDirectory out = new TDirectory()
out.mkdir('/'+OutFileName)
out.cd('/'+OutFileName)
out.addDataSet(hhel)
out.addDataSet(hphi)
out.addDataSet(hq2)
out.addDataSet(hW)

def xb_bins = 10
for(int xBi=0;xBi<=12;xBi++){
	for(int q2i=0;q2i<=16;q2i++){
		def title = "${((xBi)/xb_bins).round(2)} < xB < ${((xBi+1)/xb_bins).round(2)}_ ${q2i/2+0.0} < q2 < ${q2i/2+0.5}"
		//printer("title is $title and q2i is $q2i with ${q2i/2}",2)
		out.addDataSet(Hist_beta_p[title])
		out.addDataSet(Hist_beta_T[title])
	}
}

for(int xBi=0;xBi<=12;xBi++){
	for(int q2i=0;q2i<=16;q2i++){
		for(int xti=0;xti<=t_bins.size();xti++){
			def title = "${((xBi)/xb_bins).round(2)} < xB < ${((xBi+1)/xb_bins).round(2)}_ ${q2i/2+0.0} < q2 < ${q2i/2+0.5}"
			def low = (t_bins[xti]).toFloat().round(2)
			def high = (t_bins[xti+1]).toFloat().round(2)
			def TitleUltra = title +" " + "$low < t <  $high"
			out.addDataSet(Hist_Ultra_Phi[TitleUltra])
		}
	}
}

out.addDataSet(hxB)
out.addDataSet(heleTheta)
out.addDataSet(hproTheta)
out.addDataSet(heleproTheta)
out.addDataSet(heleproThetaDVPP)
out.addDataSet(hproThetaFD)
out.addDataSet(hproThetaCD)
out.addDataSet(H_xB_Q2)
out.addDataSet(htmom)
out.addDataSet(htmomrecon)
out.addDataSet(hLeptHadAngle)
out.writeFile(OutFileName+'.hipo')

/*Shit that does not work for trying to format axes in plots.
https://github.com/gavalian/groot/wiki/Histograms might have some more helpful info.
canvas.draw(hW);
canvas.setFont("HanziPen TC");
canvas.setTitleSize(72);
canvas.setAxisTitleSize(72);
canvas.setAxisLabelSize(76);
canvas.draw(hW);
GStyle.getAxisAttributesX().setTitleFontSize(98);
GStyle.getAxisAttributesX().setLabelFontSize(90);
EmbeddedCanvas canvas = new EmbeddedCanvas();
*/
