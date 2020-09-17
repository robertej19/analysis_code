package utils


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
import groovy.io.FileType


		//println bankParticle.getInt('status')
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


class EventProcessor {


	//NEED Q2 GREATER THAN 1
	def processEvent(event,histo_array_in,fcupBeamChargeMax) {
		println("starting to process event")

		//Unfold histograms
		def hxB = histo_array_in[0]


		//Define standard variables
		def dvpp_event = 0
		def beam = LorentzVector.withPID(11,0,0,10.6)
		def target = LorentzVector.withPID(2212,0,0,0)

		def banknames = ['REC::Event','REC::Particle','REC::Cherenkov','REC::Calorimeter','REC::Traj','REC::Track','REC::Scintillator']


		// Leave event if not all banks are present
		if(!(banknames.every{event.hasBank(it)})) {
			println("Not all bank events found, returning")
			return [fcupBeamChargeMax, dvpp_event, histo_array_in]
		}
		
		
		def (bankEvent,bankParticle,bankCherenkov,bankECal,bankTraj,bankTrack,bankScintillator) = banknames.collect{event.getBank(it)}
		def banks = [bankCherenkov:bankCherenkov,bankECal:bankECal,part:bankParticle,bankTraj:bankTraj,bankTrack:bankTrack]

		def fcupBeamCharge = bankEvent.getFloat('beamCharge',0) //This is the (un?)gated beam charge in nanoColoumbs

		if(fcupBeamCharge > fcupBeamChargeMax){ fcupBeamChargeMax = fcupBeamCharge	} //Replace fcupBeamcharge with the largest value

		def ihel = bankEvent.getByte('helicity',0) //Helicity of ... something


		//For each event, index where pid is 11 (electron) and 2212 (proton) and put into array, e.g. [[0,3]] - electron = index 0, proton = index 3
		def index_of_electrons_and_protons = (0..<bankParticle.rows()).findAll{bankParticle.getInt('pid',it)==11 && bankParticle.getShort('status',it)<0}
			.collectMany{iele->(0..<bankParticle.rows()).findAll{bankParticle.getInt('pid',it)==2212}.collect{ipro->[iele,ipro]}
		}
		println("index_of_electrons_and_protons "+index_of_electrons_and_protons)


		//Not sure exactly what this is, but returns e.g.: [[6, 7], [6, 8], [6, 9], [7, 8], [7, 9], [8, 9]] 
		def index_of_pions = (0..<bankParticle.rows()-1).findAll{bankParticle.getInt('pid',it)==22 && bankParticle.getShort('status',it)>=2000}
			.findAll{ipart_gamma_1->'xyz'.collect{bankParticle.getFloat("p$it",ipart_gamma_1)**2}.sum()>0.16}
			.collectMany{ipart_gamma_1->
			(ipart_gamma_1+1..<bankParticle.rows()).findAll{bankParticle.getInt('pid',it)==22 && bankParticle.getShort('status',it)>=2000}
			.findAll{ipart_gamma_2->'xyz'.collect{bankParticle.getFloat("p$it",ipart_gamma_2)**2}.sum()>0.16}
			.collect{ipart_gamma_2->[ipart_gamma_1,ipart_gamma_2]}
		}
		//println("index of pions is " + index_of_pions)
	
		
		//Here, we loop over all pairs of [electron, proton] in index_of_electrons_and_protons. Most of the time there is only one set, 
		//Some of the tiem there are multiple pairs, e.g. [[0,1],[0,3]]
		index_of_electrons_and_protons.findAll{iele,ipro-> //For each electron index, proton index, do the following:

			def part_electron = LorentzVector.withPID(11,*['px','py','pz'].collect{bankParticle.getFloat(it,iele)}) 
			//create a lorentz vector out of electron index. The "collect" command picks up px, py, and pz from bankParticle at index iele
			def part_proton = LorentzVector.withPID(2212,*['px','py','pz'].collect{bankParticle.getFloat(it,ipro)}) 
			//println("first electron is"+ele.e())
			//println("iele and ipro are " + ['px','py','pz'].collect{bankParticle.getFloat(it,iele)})

			def part_electron_theta = Math.toDegrees(part_electron.theta())
			def part_proton_theta = Math.toDegrees(part_proton.theta())
			if(part_proton_theta<0) part_proton_theta+=360 //Make angles be non-negative

			def wvec = beam+target-part_electron
			def qvec = beam-part_electron
			def part_X = beam+target-part_electron-part_proton
			//def t_sqrt = part_proton - target //t = (p'-p)^2
			//def t_MomTran = t_sqrt.vect().dot(t_sqrt.vect())

			//printerUtil.printer("part_X mass squared is:${part_X.mass2()}",0)
			def xBjorken = -qvec.mass2()/(2*part_proton.vect().dot(qvec.vect()))
			//printerUtil.printer("adding XB to hist "+index_of_electrons_and_protons,0)
			hxB.fill(xBjorken)


			/////////////// NO IDEA WHAT THIS IS DOING BELOW
			def pdet = (bankParticle.getShort('status',ipro)/1000).toInteger()==2 ? 'FD':'CD' 

			def part_protonfi = Math.toDegrees(part_proton.phi())
			if(part_protonfi<0) part_protonfi+=360

			def esec = (0..<bankScintillator.rows()).find{bankScintillator.getShort('pindex',it)==iele}?.with{bankScintillator.getByte('sector',it)}
			def psec = (0..<bankScintillator.rows()).find{bankScintillator.getShort('pindex',it)==ipro}?.with{bankScintillator.getByte('sector',it)}
			if(psec==0) {
				psec = Math.floor(part_protonfi/60).toInteger() +2
				if(psec==7) psec=1
			}

			def bool_ep0_event = part_X.mass2()<1 && wvec.mass()>2


			// index of pions is a set of pairs of photon indicies, it is a full permutation over all possible pairwise combinations (possible pions)
			// looping over each pair to find the "best" pion
			def pi0s = index_of_pions.collect{ipart_gamma_1,ipart_gamma_2->

				def part_gamma_1 = LorentzVector.withPID(22,*['px','py','pz'].collect{bankParticle.getFloat(it,ipart_gamma_1)})
				def part_gamma_2 = LorentzVector.withPID(22,*['px','py','pz'].collect{bankParticle.getFloat(it,ipart_gamma_2)})
				
				if(!(part_electron.vect().theta(part_gamma_1.vect())>8 && part_electron.vect().theta(part_gamma_2.vect())>8)) {
					//println("Not bool_ep0_event event, returning")
					return [fcupBeamChargeMax, dvpp_event, histo_array_in]
				}


				def gg = part_gamma_1+part_gamma_2
				def ggmass = gg.mass()
				def ispi0 = ggmass<0.2 && ggmass>0.07// && gg.p()>1.5

				if(!(ispi0)) {
					//println("Not bool_ep0_event event, returning")
					return [fcupBeamChargeMax, dvpp_event, histo_array_in]
				}
	
				def epggx = part_X-gg
				def thetaXPi = part_X.vect().theta(gg.vect())
				def dpt0 = epggx.px().abs()<0.3 && epggx.py().abs()<0.3
				def dmisse0 = epggx.e()<1
				def tt0 = -(part_proton-target).mass2()
				def part_protoncalc = beam+target-part_electron-gg
				def tt1 = -(part_protoncalc-target).mass2()


				def vLept = beam.vect().cross(part_electron.vect())
				def vHad = part_proton.vect().cross(gg.vect())
				def PlaneDot = vLept.dot(vHad)
				def cosangle = PlaneDot/vLept.mag()/vHad.mag()
				def LeptHadAngle = Math.toDegrees( Math.acos(cosangle))
				if (part_proton.vect().dot(vLept)<0){
					LeptHadAngle = -LeptHadAngle+360
				}

				def xb_bins = 10
				def q2Round = Math.round((-qvec.mass2())*2+0.5)/2
				def xBRound = Math.round(xb_bins*xBjorken+0.5)
				//printerUtil.printer("Q2 is ${-qvec.mass2()} = $q2Round and xB is $xBjorken = $xBRound",0)

				//if (q2Round < 0.4){
					//print("Q2 is low at $q2round",2)
				//}

				def title = "${((xBRound-1)/xb_bins).round(2)} < xB < ${((xBRound)/xb_bins).round(2)}_ ${q2Round - 0.5} < q2 < ${q2Round+0.0}"
				//printer("Associated title is $title",2)

				//println(t_bins)
				def tRound = (Math.round(tt0*10)/10)

				if(!(bool_ep0_event)) {
					//println("Not bool_ep0_event event, returning")
					return [fcupBeamChargeMax, dvpp_event, histo_array_in]
				}

				if(!(ispi0 && bool_ep0_event && dmisse0 && dpt0 && thetaXPi<2)) {
					//println("Not all bank events found, returning")
					return [fcupBeamChargeMax, dvpp_event, histo_array_in]
				}

				dvpp_event = 1
			}
		}

		//Set up and return arguements
		def histo_arr_out = [hxB,]
		return [fcupBeamChargeMax, dvpp_event, histo_arr_out]
	}

}