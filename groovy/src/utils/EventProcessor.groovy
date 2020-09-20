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
		
		//Number of particles
		def hist_num_protons = histo_array_in[0]
		def hist_num_photons_nocut = histo_array_in[1]
		def hist_num_photons_cut = histo_array_in[2]

		//Angle Distributions
		def hist_theta_electron_no_cuts = histo_array_in[3]
		def hist_theta_proton_no_cuts = histo_array_in[4]
		def hist_theta_proton_CD_no_cuts = histo_array_in[6]

		def hist_theta_proton_FD_no_cuts = histo_array_in[5]

		def hist_theta_proton_FD_exclu_cuts = histo_array_in[7]
		def hist_theta_proton_CD_exclu_cuts = histo_array_in[8]



		def hist_theta_proton_electron_no_cuts = histo_array_in[9]
		def hist_theta_proton_electron_FD_no_cuts = histo_array_in[10]
		def hist_theta_proton_electron_exclu_cuts = histo_array_in[11]
		def hist_theta_proton_electron_FD_exclu_cuts = histo_array_in[12]


		def hist_phi_proton_nocuts = histo_array_in[13]
		def hist_phi_proton_nocuts_FD = histo_array_in[14]
		def hist_phi_proton_nocuts_CD = histo_array_in[15]

		def hist_phi_proton_excuts = histo_array_in[16]
		def hist_phi_proton_excuts_FD = histo_array_in[17]
		def hist_phi_proton_excuts_CD = histo_array_in[18]


		//Advanced Kinematic Quantities
		def hist_xB_nocuts = histo_array_in[19]
		def hist_xB_excuts = histo_array_in[20]


		def hist_xB_Q2_excuts = histo_array_in[21]
		def hist_lept_had_angle = histo_array_in[22]


		def hist_Q2_nocuts = histo_array_in[23]
		def hist_Q2_excuts = histo_array_in[24]
		def hist_W_nocuts = histo_array_in[25]
		def hist_W_excuts = histo_array_in[26]

		def hist_t = histo_array_in[27]
		def hist_t_recon = histo_array_in[28]

		def hist_helicity = histo_array_in[29]

		def hist_theta_phi_proton_nocuts = histo_array_in[30]

		def hist_xB_Q2_FD_excuts = histo_array_in[31]
		def hist_xB_Q2_CD_excuts = histo_array_in[32]

		def hist_theta_phi_proton_nocuts_FD = histo_array_in[33]
		def hist_theta_phi_proton_nocuts_CD = histo_array_in[34]
		def hist_theta_phi_proton_excuts = histo_array_in[35]

		def hist_pion_mass_nocuts = histo_array_in[36]
		def hist_pion_mass_excuts = histo_array_in[37]

		def hist_thetaxpi_nocuts = histo_array_in[38]
		def hist_thetaxpi_excuts = histo_array_in[39]
		def hist_dmisse0	= histo_array_in[40]


		def hist_dpt0_nocuts = histo_array_in[41]
		def hist_dpt0_excuts = histo_array_in[42]

		def hist_dmisse0_excuts	= histo_array_in[43]



		// More hists









		//Define standard variables
		def dvpp_event = 0
		def xb_bins = 10
		def beam = LorentzVector.withPID(11,0,0,10.6)
		def target = LorentzVector.withPID(2212,0,0,0)

		def banknames = ['REC::Event','REC::Particle','REC::Cherenkov','REC::Calorimeter','REC::Traj','REC::Track','REC::Scintillator']


		// Leave event if not all banks are present
		if(!(banknames.every{event.hasBank(it)})) {
			//println("Not all bank events found, returning")
			return [fcupBeamChargeMax, dvpp_event, histo_array_in]
		}
		
		
		def (bankEvent,bankParticle,bankCherenkov,bankECal,bankTraj,bankTrack,bankScintillator) = banknames.collect{event.getBank(it)}
		def banks = [bankCherenkov:bankCherenkov,bankECal:bankECal,part:bankParticle,bankTraj:bankTraj,bankTrack:bankTrack]

		def fcupBeamCharge = bankEvent.getFloat('beamCharge',0) //This is the (un?)gated beam charge in nanoColoumbs

		if(fcupBeamCharge > fcupBeamChargeMax){ fcupBeamChargeMax = fcupBeamCharge	} //Replace fcupBeamcharge with the largest value

		def ihel = bankEvent.getByte('helicity',0) //Helicity of ... something
		hist_helicity.fill(ihel)


		//For each event, index where pid is 11 (electron) and 2212 (proton) and put into array, 
		//e.g. [[0,3]] - electron = index 0, proton = index 3
		def electrons_in_event = ParticleGetter.getParticle(bankParticle,"electron")
		def protons_in_event = ParticleGetter.getParticle(bankParticle,"proton")

		hist_num_protons.fill(protons_in_event.size())

		if (electrons_in_event.size() > 1){
			println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX more than 1 electron in event: ")
			println(electrons_in_event)
		}


		//Get a list of "good" photons in the event
		def good_photons_in_event = ParticleGetter.getParticle(bankParticle,"photon")
		def bad_photons_in_event = ParticleGetter.getParticle(bankParticle,"photon_raw")
		hist_num_photons_cut.fill(good_photons_in_event.size())

		hist_num_photons_nocut.fill(bad_photons_in_event.size())

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
	
				def particleProtonPhi = Math.toDegrees(particleProton.phi())
		
				if(particleProtonPhi<0) particleProtonPhi+=360

				

				def esec = (0..<bankScintillator.rows()).find{bankScintillator.getShort('pindex',it)==indexElectron}?.with{bankScintillator.getByte('sector',it)}
				def psec = (0..<bankScintillator.rows()).find{bankScintillator.getShort('pindex',it)==indexProton}?.with{bankScintillator.getByte('sector',it)}
				if(psec==0) {
					psec = Math.floor(particleProtonPhi/60).toInteger() +2
					if(psec==7) psec=1
				}

				def bool_ep0_event = particleX.mass2()<1 && wvec.mass()>2

				def proton_location = (bankParticle.getShort('status',indexProton)/1000).toInteger()==2 ? 'FD':'CD' //This returns FD if proton in FD, CD if CD


				hist_xB_nocuts.fill(xBjorken)
				hist_Q2_nocuts.fill(-qvec.mass2())
				hist_W_nocuts.fill(wvec.mass())
				hist_theta_electron_no_cuts.fill(particleElectron_theta)
				hist_theta_proton_no_cuts.fill(particleProton_theta)
				hist_theta_proton_electron_no_cuts.fill(particleProton_theta,particleElectron_theta)
				hist_phi_proton_nocuts.fill(particleProtonPhi)

				hist_theta_phi_proton_nocuts.fill(particleProtonPhi,particleProton_theta)


				if (proton_location == 'FD'){
					hist_theta_proton_FD_no_cuts.fill(particleProton_theta)
					hist_phi_proton_nocuts_FD.fill(particleProtonPhi)
					hist_theta_proton_electron_FD_no_cuts.fill(particleProton_theta,particleElectron_theta)
					hist_theta_phi_proton_nocuts_FD.fill(particleProtonPhi,particleProton_theta)

				}
				if (proton_location == 'CD'){
					hist_theta_proton_CD_no_cuts.fill(particleProton_theta)
					hist_phi_proton_nocuts_CD.fill(particleProtonPhi)
					hist_theta_phi_proton_nocuts_CD.fill(particleProtonPhi,particleProton_theta)
				}


		

				// index of pions is a set of pairs of photon indicies, it is a full permutation over all possible pairwise combinations (possible pions)
				// looping over each pair to find the "best" pion


				for (gamma_pair in photon_perms){
					def indexParticleGamma_1 = gamma_pair[0]
					def indexParticleGamma_2 = gamma_pair[1]

					def particleGamma_1 = LorentzVector.withPID(22,*['px','py','pz'].collect{bankParticle.getFloat(it,indexParticleGamma_1)})
					def particleGamma_2 = LorentzVector.withPID(22,*['px','py','pz'].collect{bankParticle.getFloat(it,indexParticleGamma_2)})
					
					

					def particleGammaGammaPair = particleGamma_1+particleGamma_2
					def particleGammaGammaPairmass = particleGammaGammaPair.mass()


					hist_pion_mass_nocuts.fill(particleGammaGammaPairmass*1000) //Report the mass in MeV

					def ispi0 = particleGammaGammaPairmass<0.2 && particleGammaGammaPairmass>0.07 && particleGammaGammaPair.p()>1.5

					

					//************ Now we define DVEP exclusive cuts **********************
		
					//Below we calculate the angle (in degrees) between the missing 4 momentum of the ep-->ep system (X)
					//And the 4 momentum of the reconstructed pion. 
					def thetaXPi = particleX.vect().theta(particleGammaGammaPair.vect())
					//Below is the boolean cut, requiring the angle between X and the Pion to be less than 2 degrees
					def excut_thetaXPi = thetaXPi<2
				

					//Here we define the difference between the 4 vectors of teh pion and X 4 vectors
					def diff_between_X_and_GG = particleX-particleGammaGammaPair

					//Here we define a boolean cut - need the difference in transverse momentum of 
					//To be less than 300 MeV in each direction
					def excut_dpt0 = diff_between_X_and_GG.px().abs()<0.3 && diff_between_X_and_GG.py().abs()<0.3
					
					def dmisse0 = diff_between_X_and_GG.e()
					def excut_dmisse0 = dmisse0<1

					

					if(!(ispi0)) { continue	}
					if(!(particleElectron.vect().theta(particleGamma_1.vect())>8 && particleElectron.vect().theta(particleGamma_2.vect())>8)) { continue }


					
					//Fill related histograms
					hist_dpt0_nocuts.fill(diff_between_X_and_GG.px().abs()*1000,diff_between_X_and_GG.py().abs()*1000)
					hist_thetaxpi_nocuts.fill(thetaXPi)
					hist_dmisse0.fill(dmisse0)

					//*********************************************************************

					//************ Define other kinematic quantities **********************
					//*********************************************************************

					def t_momentum = -(particleProton-target).mass2() //This is the kinematic variable t
					def particleProtoncalc = beam+target-particleElectron-particleGammaGammaPair
					def t_momentum_recon = -(particleProtoncalc-target).mass2() //This is the kinematic variable t, calculated differently



					
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
					def tRound = (Math.round(t_momentum*10)/10)

					if(!(bool_ep0_event)) { continue }

					if(!(ispi0 && bool_ep0_event && excut_dmisse0 && excut_dpt0 && excut_thetaXPi)) { continue}

					//IF WE HAVE MADE IT THIS FAR, WE NOW HAVE A DVEP EVENT!!!!!!!!

					//fill histograms
					
					hist_theta_proton_electron_exclu_cuts.fill(particleProton_theta,particleElectron_theta)
					if (proton_location == 'FD'){
						hist_theta_proton_FD_exclu_cuts.fill(particleProton_theta)
						hist_phi_proton_excuts_FD.fill(particleProtonPhi)
						hist_theta_proton_electron_FD_exclu_cuts.fill(particleProton_theta,particleElectron_theta)
						hist_xB_Q2_FD_excuts.fill(xBjorken,-qvec.mass2())
					}
					if (proton_location == 'CD'){
						hist_theta_proton_CD_exclu_cuts.fill(particleProton_theta)
						hist_phi_proton_excuts_CD.fill(particleProtonPhi)
						hist_xB_Q2_CD_excuts.fill(xBjorken,-qvec.mass2())

					}
					
					hist_dpt0_excuts.fill(diff_between_X_and_GG.px().abs()*1000,diff_between_X_and_GG.py().abs()*1000)
					hist_thetaxpi_excuts.fill(thetaXPi)
					hist_pion_mass_excuts.fill(particleGammaGammaPairmass*1000)
					hist_theta_phi_proton_excuts.fill(particleProtonPhi,particleProton_theta)
					hist_phi_proton_excuts.fill(particleProtonPhi)
					hist_xB_excuts.fill(xBjorken)
					hist_Q2_excuts.fill(-qvec.mass2())
					hist_W_excuts.fill(wvec.mass())
					hist_xB_Q2_excuts.fill(xBjorken,-qvec.mass2())
					hist_lept_had_angle.fill(LeptHadAngle)
					hist_t.fill(t_momentum)
					hist_t_recon.fill(t_momentum_recon)
					hist_dmisse0_excuts.fill(dmisse0)


					
					

					dvpp_event = 1
				}
			}
		}

		//Set up and return arguements
		def histo_arr_out = [hist_num_protons, hist_num_photons_nocut,hist_num_photons_cut,
					hist_theta_electron_no_cuts,hist_theta_proton_no_cuts,hist_theta_proton_FD_no_cuts,
					hist_theta_proton_CD_no_cuts,hist_theta_proton_FD_exclu_cuts,hist_theta_proton_CD_exclu_cuts,
					hist_theta_proton_electron_no_cuts,hist_theta_proton_electron_FD_no_cuts,
					hist_theta_proton_electron_exclu_cuts,hist_theta_proton_electron_FD_exclu_cuts,
					hist_phi_proton_nocuts,hist_phi_proton_nocuts_FD,hist_phi_proton_nocuts_CD,hist_phi_proton_excuts,
					hist_phi_proton_excuts_FD,hist_phi_proton_excuts_CD,hist_xB_nocuts,hist_xB_excuts,
					hist_xB_Q2_excuts,hist_lept_had_angle,hist_Q2_nocuts,hist_Q2_excuts,hist_W_nocuts,hist_W_excuts,
					hist_t,hist_t_recon,hist_helicity,hist_theta_phi_proton_nocuts,
					hist_xB_Q2_FD_excuts, hist_xB_Q2_CD_excuts,
					hist_theta_phi_proton_nocuts_FD, hist_theta_phi_proton_nocuts_CD, hist_theta_phi_proton_excuts,
					hist_pion_mass_nocuts,hist_pion_mass_excuts,
					hist_thetaxpi_nocuts, hist_thetaxpi_excuts,hist_dmisse0,
					hist_dpt0_nocuts,hist_dpt0_excuts,
					hist_dmisse0_excuts]

		return [fcupBeamChargeMax, dvpp_event, histo_arr_out]
	}

}