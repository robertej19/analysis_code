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

//From Local
import utils.subutils.ParticleGetter
import utils.subutils.PermutationMaker


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
	def processEvent(j,event,histo_array_in,fcupBeamChargeMax) {
		//println("starting to process event")

		
		//Unfold histograms
		def hxB = histo_array_in[0]


		//Define standard variables
		def dvpp_event = 0
		def xb_bins = 10
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


		//For each event, index where pid is 11 (electron) and 2212 (proton) and put into array, 
		//e.g. [[0,3]] - electron = index 0, proton = index 3
		def electrons_in_event = ParticleGetter.getParticle(bankParticle,"electron")
		def protons_in_event = ParticleGetter.getParticle(bankParticle,"proton")


		//Get a list of "good" photons in the event
		def good_photons_in_event = ParticleGetter.getParticle(bankParticle,"photon")
		//Create a set of all possible pairwise permutations of the photons (need 2 photons for pion)
		def photon_perms = PermutationMaker.makePermutations(good_photons_in_event)

		
		//Here, we loop over all pairs of [electron, proton] in index_of_electrons_and_protons. Most of the time there is only one set, 
		//Some of the tiem there are multiple pairs, e.g. [[0,1],[0,3]]
		for (int indexElectron in electrons_in_event){
			for (int indexProton in protons_in_event){ //For each electron index, proton index, do the following:

				def particleElectron = LorentzVector.withPID(11,*['px','py','pz'].collect{bankParticle.getFloat(it,indexElectron)}) 
				//create a lorentz vector out of electron index. The "collect" command picks up px, py, and pz from bankParticle at index indexElectron
				def particleProton = LorentzVector.withPID(2212,*['px','py','pz'].collect{bankParticle.getFloat(it,indexProton)}) 
				//println("first electron is"+ele.e())
				//println("indexElectron and indexProton are " + ['px','py','pz'].collect{bankParticle.getFloat(it,indexElectron)})

				def particleElectron_theta = Math.toDegrees(particleElectron.theta())
				def particleProton_theta = Math.toDegrees(particleProton.theta())
				if(particleProton_theta<0) particleProton_theta+=360 //Make angles be non-negative

				def wvec = beam+target-particleElectron
				def qvec = beam-particleElectron
				def particleX = beam+target-particleElectron-particleProton
				//def t_sqrt = particleProton - target //t = (p'-p)^2
				//def t_MomTran = t_sqrt.vect().dot(t_sqrt.vect())

				//printerUtil.printer("particleX mass squared is:${particleX.mass2()}",0)
				def xBjorken = -qvec.mass2()/(2*particleProton.vect().dot(qvec.vect()))
				//printerUtil.printer("adding XB to hist "+index_of_electrons_and_protons,0)
				hxB.fill(xBjorken)


				/////////////// NO IDEA WHAT THIS IS DOING BELOW
				def pdet = (bankParticle.getShort('status',indexProton)/1000).toInteger()==2 ? 'FD':'CD' 
				///////// FIGURE THIS OUT ABOVE

				def particleProtonfi = Math.toDegrees(particleProton.phi())
				if(particleProtonfi<0) particleProtonfi+=360

				def esec = (0..<bankScintillator.rows()).find{bankScintillator.getShort('pindex',it)==indexElectron}?.with{bankScintillator.getByte('sector',it)}
				def psec = (0..<bankScintillator.rows()).find{bankScintillator.getShort('pindex',it)==indexProton}?.with{bankScintillator.getByte('sector',it)}
				if(psec==0) {
					psec = Math.floor(particleProtonfi/60).toInteger() +2
					if(psec==7) psec=1
				}

				def bool_ep0_event = particleX.mass2()<1 && wvec.mass()>2


				// index of pions is a set of pairs of photon indicies, it is a full permutation over all possible pairwise combinations (possible pions)
				// looping over each pair to find the "best" pion


				for (gamma_pair in photon_perms){
					def indexParticleGamma_1 = gamma_pair[0]
					def indexParticleGamma_2 = gamma_pair[1]

					def particleGamma_1 = LorentzVector.withPID(22,*['px','py','pz'].collect{bankParticle.getFloat(it,indexParticleGamma_1)})
					def particleGamma_2 = LorentzVector.withPID(22,*['px','py','pz'].collect{bankParticle.getFloat(it,indexParticleGamma_2)})
					
					if(!(particleElectron.vect().theta(particleGamma_1.vect())>8 && particleElectron.vect().theta(particleGamma_2.vect())>8)) { continue }


					def particleGammaGammaPair = particleGamma_1+particleGamma_2
					def particleGammaGammaPairmass = particleGammaGammaPair.mass()
					def ispi0 = particleGammaGammaPairmass<0.2 && particleGammaGammaPairmass>0.07// && particleGammaGammaPair.p()>1.5

					if(!(ispi0)) { continue	}
		
					def diff_between_X_and_GG = particleX-particleGammaGammaPair
					def thetaXPi = particleX.vect().theta(particleGammaGammaPair.vect())
					def dpt0 = diff_between_X_and_GG.px().abs()<0.3 && diff_between_X_and_GG.py().abs()<0.3
					def dmisse0 = diff_between_X_and_GG.e()<1
					def tt0 = -(particleProton-target).mass2()
					def particleProtoncalc = beam+target-particleElectron-particleGammaGammaPair
					def tt1 = -(particleProtoncalc-target).mass2()


					def vLept = beam.vect().cross(particleElectron.vect())
					def vHad = particleProton.vect().cross(particleGammaGammaPair.vect())
					def PlaneDot = vLept.dot(vHad)
					def cosangle = PlaneDot/vLept.mag()/vHad.mag()
					def LeptHadAngle = Math.toDegrees( Math.acos(cosangle))
					if (particleProton.vect().dot(vLept)<0){
						LeptHadAngle = -LeptHadAngle+360
					}

					
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

					if(!(bool_ep0_event)) { continue }

					if(!(ispi0 && bool_ep0_event && dmisse0 && dpt0 && thetaXPi<2)) { continue}
					
					dvpp_event = 1
				}
			}
		}

		//Set up and return arguements
		def histo_arr_out = [hxB,]
		return [fcupBeamChargeMax, dvpp_event, histo_arr_out]
	}

}