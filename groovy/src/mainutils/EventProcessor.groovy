package mainutils


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


import org.jlab.clas.physics.Particle
import org.jlab.io.hipo.HipoDataSource

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
import mainutils.subutils.ParticleGetter
import mainutils.subutils.PermutationMaker
import mainutils.subutils.PionCutter
import mainutils.subutils.DVEPCutter


//OTHER
import org.jlab.io.hipo.HipoDataSource
import org.jlab.clas.physics.LorentzVector
import uconn.utils.pid.stefan.ElectronCandidate
import uconn.utils.pid.stefan.ElectronCandidate.Cut
import uconn.utils.pid.stefan.ProtonCandidate
import uconn.utils.pid.stefan.ProtonCandidate.Cut

import mainutils.pid.electron.ElectronFromEvent
import mainutils.event.Event
import mainutils.event.EventConverter
import mainutils.utils.KinTool
import mainutils.pid.electron.ElectronSelector
import mainutils.pid.proton.ProtonFromEvent
import mainutils.pid.proton.ProtonSelector
import mainutils.pid.gamma.GammaFromEvent
import mainutils.pid.gamma.GammaSelector


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


	def processEvent(j,event0,hist_array_in,fcupBeamChargeMax,cuts_array,binning_scheme,cut_strats,part_selectors,custom_PID) {
		//println("starting to process event0")

		
		def null_return_array = ["null","null"]
		def return_array = null_return_array
		//Unfold histgrams
		

		//Define standard variables

		def binning_xb = binning_scheme[0]
		def binning_q2 = binning_scheme[1]
		def binning_t = binning_scheme[2]

		def dvpp_event = 0
		def fd_dvpp_event = 0 
		def cd_dvpp_event = 0
		def fd_all_event = 0 
		def cd_all_event = 0
		def num_electrons = 0

		def xb_bins = 10
		def beam = LorentzVector.withPID(11,0,0,10.6041)
		def target = LorentzVector.withPID(2212,0,0,0)

		def hist_array_out = []



		def banknames = ['REC::Event','REC::Particle','REC::Cherenkov','REC::Calorimeter','REC::Traj','REC::Track','REC::Scintillator','RUN::config']

		//println("might leave")

		// Leave event if not all banks are present
		if(!(banknames.every{event0.hasBank(it)})) {
			//println("Not all bank events found, returning")
			return [fcupBeamChargeMax, dvpp_event, fd_dvpp_event, cd_dvpp_event, fd_all_event, cd_all_event, hist_array_in, return_array,1,1]
		}
		
		//println("did not leave")

		

		
		def (bankEvent,bankParticle,bankCherenkov,bankECal,bankTraj,bankTrack,bankScintillator,bankRun) = banknames.collect{event0.getBank(it)}
		def banks = [bankCherenkov:bankCherenkov,bankECal:bankECal,part:bankParticle,bankTraj:bankTraj,bankTrack:bankTrack,bankRun:bankRun]

		def fcupBeamCharge = bankEvent.getFloat('beamCharge',0) //This is the (un?)gated beam charge in nanoColoumbs

		def event_number = bankRun.getInt('event',0)



		//println(event_number)

		if(fcupBeamCharge > fcupBeamChargeMax){ fcupBeamChargeMax = fcupBeamCharge	} //Replace fcupBeamcharge with the largest value

		def i_helicity = bankEvent.getByte('helicity',0) //Helicity
		

		//For each event0, index where pid is 11 (electron) and 2212 (proton) and put into array, 
		//e.g. [[0,3]] - electron = index 0, proton = index 3


	//The below is all depreciated after implementing cuts from drejenko

		def good_photons_in_event = []
		def electrons_in_event = []
		def protons_in_event = []


		if (custom_PID == 0 ){

			//print("not using custom PID")
		

			electrons_in_event = ParticleGetter.getParticle(bankParticle,bankECal,"electron",0)
			protons_in_event = ParticleGetter.getParticle(bankParticle,bankECal,"proton",0)


			//Electrons are trigger particle, so have index 0
			def ele_part_ind = 0
			//Get a list of "good" photons in the event0
			def esec0 = (0..<bankScintillator.rows()).find{bankScintillator.getShort('pindex',it)==ele_part_ind}?.with{bankScintillator.getByte('sector',it)}
			good_photons_in_event = ParticleGetter.getParticle(bankParticle,bankECal,"photon",esec0)

			//def bad_photons_in_event = ParticleGetter.getParticle(bankParticle,"photon_raw")

		}

//******************************* IMPLEMENT NEW PID METHODS

		if (custom_PID == 1 ){
			//println("using custom PID")


			// def myElectronCutStrategies = cut_strats[0]
			// def myProtonCutStrategies = cut_strats[1]
			// def myGammaCutStrategies = cut_strats[2]


			// def ele_selector = part_selectors[0]
			// def pro_selector = part_selectors[1]
			// def gam_selector = part_selectors[2]




			// def event = EventConverter.convert(event0)

			// // first method for selecting electrons illustrates what the ElectronSelector class is doing under the hood.
			// def my_el_cuts = (0..<event.npart).findAll{event.charge[it]<0}.collect{ ii -> [ii, myElectronCutStrategies.collect{ el_test -> el_test(event,ii) } ] }.collectEntries()	  
			// my_el_cuts.each{ index, value ->
			// 	if (!value.contains(false)){
			// 		def lv = new Vector3(event.px[index], event.py[index], event.pz[index])
			// 		def p = lv.mag()
			// 		def vz = event.vz[index]
			// 		def theta = Math.toDegrees(lv.theta())
			// 		def phi = Math.toDegrees(lv.phi())
			// 		//do other stuff here
			// 	}
			// }

			// // second method here will return a list of indicies for tracks passing all electron cuts.
			// // decide which of the electron candidates you want here
			// def my_good_el = ele_selector.getGoodElectron(event)	
			// // third method will return a map with key as the REC::Particle index and value as the list of booleans describing if the track passed the cut or not.
			// def my_good_el_with_cuts = ele_selector.getGoodElectronWithCuts(event)

			// // // do the same for proton
			// def my_pro_cuts = (0..<event.npart).findAll{event.charge[it]>0}.collect{ ii -> [ii, myProtonCutStrategies.collect{ pro_test -> pro_test(event,ii) } ] }.collectEntries()	  
			// my_pro_cuts.each{ index, value ->
			// 	if (!value.contains(false)){
			// 		def lv = new Vector3(event.px[index], event.py[index], event.pz[index])
			// 		def p = lv.mag()
			// 		def vz = event.vz[index]
			// 		def theta = Math.toDegrees(lv.theta())
			// 		def phi = Math.toDegrees(lv.phi())
			// 		//do other stuff here
			// 	}
			// }

			// def my_good_pro = pro_selector.getGoodProton(event)	
			// def my_good_pro_with_cuts = pro_selector.getGoodProtonWithCuts(event)

			// // do the same for gamma
			// def my_gam_cuts = (0..<event.npart).findAll{event.charge[it]==0}.collect{ ii -> [ii, myGammaCutStrategies.collect{ gam_test -> gam_test(event,ii) } ] }.collectEntries()	  
			// my_gam_cuts.each{ index, value ->
			// 	if (!value.contains(false)){
			// 		def lv = new Vector3(event.px[index], event.py[index], event.pz[index])
			// 		def p = lv.mag()
			// 		def vz = event.vz[index]
			// 		def theta = Math.toDegrees(lv.theta())
			// 		def phi = Math.toDegrees(lv.phi())
			// 		//do other stuff here
			// 	}
			// }

			// def my_good_gam = gam_selector.getGoodGamma(event)	
			// def my_good_gam_with_cuts = gam_selector.getGoodGammaWithCuts(event)




			// good_photons_in_event = my_good_gam
			// //println(my_good_gam)
			// //electrons_in_event = my_good_el
			// //protons_in_event = my_good_pro



		}


		if (custom_PID == 2){


			def ele_part_ind = 0
			def candidate_E = ElectronCandidate.getElectronCandidate(ele_part_ind, bankParticle, bankECal, bankCherenkov, bankTraj)
			if(candidate_E.iselectron()){
				 electrons_in_event.add(ele_part_ind)
			}


			for (int ipart in 0..<bankParticle.rows()){
                def candidate_proton = ProtonCandidate.getProtonCandidate(ipart, bankParticle, bankTraj)

                //test for all cuts specified in ElectronCandidate class
                if(candidate_proton.isproton(Cut.values())) {
                    //println("good electron is found")
                    protons_in_event.add(ipart)
                    //println("electron momentum = "+ele.p())
                }
            }


			def esec0 = (0..<bankScintillator.rows()).find{bankScintillator.getShort('pindex',it)==ele_part_ind}?.with{bankScintillator.getByte('sector',it)}
			good_photons_in_event = ParticleGetter.getParticle(bankParticle,bankECal,"photon",esec0)


		}



	//Create a set of all possible pairwise permutations of the photons (need 2 photons for pion)
		def photon_perms = PermutationMaker.makePermutations(good_photons_in_event)


		if (1603045 == event_number){
			println("ON RIGHT EVENT NUMBER $event_number")
			println(electrons_in_event)
			println(protons_in_event)
			println(good_photons_in_event)
			
		}

//XXXXXXXXXXXXXXSWITC




//***************************************** 














		/////////////////////////////////////////////
		// FIRST HISTOGRAM FILL SPOT //////////////
		/////////////////////////////////////////////////


		def variable_map_pre_fill = ["number_photons_good":good_photons_in_event.size(),"number_photons_bad":good_photons_in_event.size(),
			"number_protons":protons_in_event.size(),"helicity":i_helicity]

		//println("pre-filling histograms")
		//Fill pre-fill histgrams
		for (int hist_couplet_index=0; hist_couplet_index < hist_array_in.size(); hist_couplet_index++){
			//unpack
			def hist_couplet = hist_array_in[hist_couplet_index]
			def hist_params = hist_couplet[0]
			def hist_mini_array = hist_couplet[1]	
			if (hist_params.get("pre_fill") == "yes"){	

				def all_index = 0
				def variable_map = variable_map_pre_fill
									
				def fillvars = [variable_map.get(hist_params.get("fill_x")),]	

			
				if(hist_params.get("num_bins_z") > 0){ fillvars.add(variable_map.get(hist_params.get("fill_z")))	}

				//println(hist_params.get("fill_x"))
				//println(fillvars)
				//Fill histos
				hist_mini_array[all_index].fill(fillvars) //Fill "All" histogram						
				//Repack
				hist_array_in[hist_couplet_index] = [hist_params,hist_mini_array]
			}
		}



		// def only2photons = good_photons_in_event.size() > 1
		// def only2photons2 = good_photons_in_event.size() <3
		// def only1proton = protons_in_event.size() >0
		// def only1proton2 = protons_in_event.size() <2

		//if(!(only1proton2 && only1proton && only2photons2 && only2photons)) {
			//println("value is $only1proton2 $only1proton $only2photons2 $only2photons")
			//println(good_photons_in_event.size())
			//println(protons_in_event.size())
		//	return [fcupBeamChargeMax, dvpp_event, fd_dvpp_event, cd_dvpp_event, fd_all_event, cd_all_event, hist_array_in]
		//}

		//println(electrons_in_event.size())
		//println(protons_in_event.size())

		
		//Here, we loop over all pairs of [electron, proton] in index_of_electrons_and_protons. Most of the time there is only one set, 
		//Some of the tiem there are multiple pairs, e.g. [[0,1],[0,3]]

		def duplCounter = 0

		//println("NUM Protons IN event")
		//println(protons_in_event.size())
		//println("NUM eles IN event")
		//println(electrons_in_event.size())

		
		num_electrons = electrons_in_event.size()

		for (int indexElectron in electrons_in_event){
			for (int indexProton in protons_in_event){ 
				dvpp_event = 0
				fd_dvpp_event = 0 
				cd_dvpp_event = 0
				fd_all_event = 0 
				cd_all_event = 0
				//println("IN GOOD AREA, values"+only1proton2)only1proton, only2photons2, only2photons)
				//println("value is $only1proton2 $only1proton $only2photons2 $only2photons")
				
				//For each electron index, proton index, do the following:

				def particleElectron = LorentzVector.withPID(11,*['px','py','pz'].collect{bankParticle.getFloat(it,indexElectron)}) 
				//create a lorentz vector out of electron index. The "collect" command picks up px, py, and pz from bankParticle at index indexElectron
				def particleProton = LorentzVector.withPID(2212,*['px','py','pz'].collect{bankParticle.getFloat(it,indexProton)}) 
				//println("first electron is"+ele.e())
				//println("indexElectron and indexProton are " + ['px','py','pz'].collect{bankParticle.getFloat(it,indexElectron)})

				def particleElectronTheta = Math.toDegrees(particleElectron.theta())
				def particleProtonTheta = Math.toDegrees(particleProton.theta())
				if(particleProtonTheta<0) particleProtonTheta+=360 //Make angles be non-negative


				def wvec = beam+target-particleElectron
				def qvec = beam-particleElectron
				
				def qsquared = -qvec.mass2()
				def particleX = beam+target-particleElectron-particleProton
				//def t_sqrt = particleProton - target //t = (p'-p)^2
				//def t_MomTran = t_sqrt.vect().dot(t_sqrt.vect())

				//printerUtil.printer("particleX mass squared is:${particleX.mass2()}",0)
				//def xBjorkenBad = qsquared/(2*particleProton.vect().dot(qvec.vect()))
				def xBjorkenBad = 0 //this needs to be removed from code as it is legacy, but not harmful now
				def xBjorken = qsquared/(qsquared +wvec.mass2() - particleProton.mass2())
				//printerUtil.printer("adding XB to hist "+index_of_electrons_and_protons,0)
	
				def particleProtonPhi = Math.toDegrees(particleProton.phi())
				if(particleProtonPhi<0) particleProtonPhi+=360

				def particleElectronPhi = Math.toDegrees(particleElectron.phi())
				if(particleElectronPhi<0) particleElectronPhi+=360
				

				//This is currently unused, I think for handling sectors
				def esec = (0..<bankScintillator.rows()).find{bankScintillator.getShort('pindex',it)==indexElectron}?.with{bankScintillator.getByte('sector',it)}
				def psec = (0..<bankScintillator.rows()).find{bankScintillator.getShort('pindex',it)==indexProton}?.with{bankScintillator.getByte('sector',it)}
				
				//println(esec)

				def electron_sector = esec
				if(psec==0) {
					psec = Math.floor(particleProtonPhi/60).toInteger() +2
					if(psec==7) psec=1
				}
				//Finishes comments

				def proton_location = (bankParticle.getShort('status',indexProton)/1000).toInteger()==2 ? 'FD':'CD' //This returns FD if proton in FD, CD if CD

				//println("proton info")
				//println(bankParticle.getShort('status',indexProton))
				//println(proton_location)


				//println(bankParticle.getShort('status',indexProton))
				//println(proton_location)




				if (proton_location == 'FD'){ fd_all_event = 1	}
				if (proton_location == 'CD'){ cd_all_event = 1	}
				
				


				def t_momentum = -(particleProton-target).mass2() //This is the kinematic variable t (needs to be squared?)

				
				def q2Round = Math.round((-qvec.mass2())*2+0.5)/2
				def xBRound = Math.round(xb_bins*xBjorken+0.5)
				def tRound = (Math.round(t_momentum*10)/10)
				

				if (3483962 == event_number){
					println("ON STAGE TWO RIGHT EVENT NUMBER $event_number")
					println(proton_location)
					println(t_momentum)
					println(xBjorken)
					println((-qvec.mass2()))
					
				}

				def variable_map_nocuts = ["particleProtonTheta":particleProtonTheta,"particleElectronTheta":particleElectronTheta,
								"particleProtonPhi":particleProtonPhi,"particleElectronPhi":particleElectronPhi,
								"t_momentum":t_momentum,
								"q2":-qvec.mass2(),"xb":xBjorken, "xbbad":xBjorkenBad,
								 "w_vector":wvec.mass(),
								"particleProtonMass":particleProton.mass(),
								"number_photons_good":good_photons_in_event.size(),"number_photons_bad":good_photons_in_event.size(),
								"number_protons":protons_in_event.size(),"electron_sector":electron_sector
								]


				def title_xB = ""
				def title_q2 = ""
				def title_t = ""
				def title_xbq2t = ""
				def title_xbq2 = ""
				
				for(int xbi=0;xbi<binning_xb.size()-1;xbi++){
					if(binning_xb[xbi]<xBjorken && xBjorken<binning_xb[xbi+1]){
						def lowxb = (binning_xb[xbi]).toFloat().round(3)
						def highxb = (binning_xb[xbi+1]).toFloat().round(3)
						title_xB += " $lowxb < xB < $highxb, "
					}
				}

				for(int q2i=0;q2i<binning_q2.size()-1;q2i++){
					if(binning_q2[q2i] < qsquared && qsquared < binning_q2[q2i+1]){
						def lowq2 = (binning_q2[q2i]).toFloat().round(3)
						def highq2 = (binning_q2[q2i+1]).toFloat().round(3)
						title_q2 += "$lowq2 < q2 < $highq2, "
					}
				}

				for(int ti=0;ti<binning_t.size()-1;ti++){
					if(binning_t[ti] < t_momentum && t_momentum < binning_t[ti+1]){
						def lowt = (binning_t[ti]).toFloat().round(3)
						def hight = (binning_t[ti+1]).toFloat().round(3)
						title_t += "$lowt < t < $hight"
					}
				}

				title_xbq2t += title_xB+title_q2+title_t
				title_xbq2 += title_xB+title_q2


				/////////////////////////////////////////////
				// SECOND HISTOGRAM FILL SPOT //////////////
				/////////////////////////////////////////////////



				//Fill nocut histgrams
				for (int hist_couplet_index=0; hist_couplet_index < hist_array_in.size(); hist_couplet_index++){
					//unpack
					def hist_couplet = hist_array_in[hist_couplet_index]
					def hist_params = hist_couplet[0]
					def hist_mini_array = hist_couplet[1]	
					if (hist_params.get("pro_fill") == "yes"){	

						def all_index = 5
						def fd_index = 4
						def cd_index = 3
						def variable_map = variable_map_nocuts
											
						def fillvars = [variable_map.get(hist_params.get("fill_x")),]	
						if(hist_params.get("num_bins_z") > 0){ fillvars.add(variable_map.get(hist_params.get("fill_z")))	}

						//println(hist_params.get("fill_x"))
					//	println(fillvars)
						//Fill histos
						hist_mini_array[all_index].fill(fillvars) //Fill "All" histogram
						if (proton_location == 'FD'){ hist_mini_array[fd_index].fill(fillvars)	} //Fill FD
						if (proton_location == 'CD'){hist_mini_array[cd_index].fill(fillvars)	} //Fill CD
												
						//Repack
						hist_array_in[hist_couplet_index] = [hist_params,hist_mini_array]
					}
				}



				// index of pions is a set of pairs of photon indicies, it is a full permutation over all possible pairwise combinations (possible pions)
				// looping over each pair to find the "best" pion


				for (gamma_pair in photon_perms){
					def indexParticleGamma_1 = gamma_pair[0]
					def indexParticleGamma_2 = gamma_pair[1]


					def pho1sec = (0..<bankECal.rows()).find{bankECal.getShort('pindex',it)==indexParticleGamma_1}?.with{bankECal.getByte('sector',it)}
					def pho2sec = (0..<bankECal.rows()).find{bankECal.getShort('pindex',it)==indexParticleGamma_2}?.with{bankECal.getByte('sector',it)}
				
					//					println("photon1 sector is "+pho1sec)
					//					println("photon2 sector is "+pho2sec)


					if (pho1sec == null){
						//println("pho1sec is null, changing to match electron sector")
						pho1sec = electron_sector
					}
					if (pho2sec == null){
						//println("pho2sec is null, changing to match electron sector")
						pho2sec = electron_sector
					}

					def particleGamma_1 = LorentzVector.withPID(22,*['px','py','pz'].collect{bankParticle.getFloat(it,indexParticleGamma_1)})
					def particleGamma_2 = LorentzVector.withPID(22,*['px','py','pz'].collect{bankParticle.getFloat(it,indexParticleGamma_2)})
					
					

					def particleGammaGammaPair = particleGamma_1+particleGamma_2
					def particleGammaGammaPairmass = particleGammaGammaPair.mass()

					def particle0 = beam+target-particleElectron-particleProton-particleGammaGammaPair
					

					//hist_pion_mass_nocuts.fill(particleGammaGammaPairmass*1000) //Report the mass in MeV


					def pion_cuts = [particleGamma_1,particleGamma_2,particleElectron, electron_sector, pho1sec, pho2sec]
					def ispi0 = PionCutter.cutPions(pion_cuts,cuts_array)


					if (3483962 == event_number){
						println("ON STAGE Three RIGHT EVENT NUMBER $event_number")
						println(pho1sec)
						println(pho2sec)
						println(electron_sector)
						println(ispi0)
						
					}

					//************ Now we define DVEP exclusive cuts **********************
		
					//Below we calculate the angle (in degrees) between the missing 4 momentum of the ep-->ep system (X)
					//And the 4 momentum of the reconstructed pion. 
					def thetaXPi = particleX.vect().theta(particleGammaGammaPair.vect())
					//Below is the boolean cut, requiring the angle between X and the Pion to be less than 2 degrees

					//Here we define the difference between the 4 vectors of teh pion and X 4 vectors
					def diff_between_X_and_GG = particleX-particleGammaGammaPair

					//Here we define a boolean cut - need the difference in transverse momentum of 
					//To be less than 300 MeV in each direction

					def dmisse0 = diff_between_X_and_GG.e()
		
					
					//Fill related histgrams
				/*	hist_dpt0_nocuts.fill(diff_between_X_and_GG.px().abs()*1000,diff_between_X_and_GG.py().abs()*1000)
					hist_thetaxpi_nocuts.fill(thetaXPi)
					hist_dmisse0.fill(dmisse0)

					hist_miss_e_mass_nocuts.fill(particleX.mass(),particleX.e())
					hist_missing_e_nocuts.fill(particleX.e())
					hist_x_mass_nocuts.fill(particleX.mass())
					*/

					//*********************************************************************

					//************ Define other kinematic quantities **********************
					//*********************************************************************

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

					def LeptHadAngle_pos = LeptHadAngle
					def LeptHadAngle_neg = -1000

					if (i_helicity < 0){
						LeptHadAngle_neg = LeptHadAngle
						LeptHadAngle_pos = -1000
					}
						


					def variable_map_excuts = ["particleProtonTheta":particleProtonTheta,"particleElectronTheta":particleElectronTheta,
					"particleProtonPhi":particleProtonPhi,"particleElectronPhi":particleElectronPhi,
					"particlePionEnergy":particleGammaGammaPair.e(),"particlePionMass":particleGammaGammaPairmass*1000,
					"particleXEnergy":particleX.e(),"particleXMassSquared":particleX.mass2(),
					"momentumDiffX":diff_between_X_and_GG.px().abs()*1000,"momentumDiffY":diff_between_X_and_GG.py().abs()*1000,
					"missingEnergyDifference":dmisse0,
					"thetaXPi":thetaXPi,"t_momentum":t_momentum,"t_momentum_recon":t_momentum_recon,
					"q2":-qvec.mass2(),"xb":xBjorken, "LeptHadAngle":LeptHadAngle,"LeptHadAngle_pos":LeptHadAngle_pos, "LeptHadAngle_neg":LeptHadAngle_neg, "w_vector":wvec.mass(),
					"particleProtonMass":particleProton.mass(),
					"particle0Energy":particle0.e(),"particle0MassSquared":particle0.mass2(),
					 "xbbad":xBjorkenBad,"number_photons_good":good_photons_in_event.size(),"number_photons_bad":good_photons_in_event.size(),
					"number_protons":protons_in_event.size(),"electron_sector":electron_sector,
					"pho1sec":pho1sec, "pho2sec":pho2sec
					]

					//println("particle0 energy is "+particle0.e())
					//println("particle0 mass is "+particle0.mass())

					/////////////////////////////////////////////
					// THIRD HISTOGRAM FILL SPOT //////////////
					/////////////////////////////////////////////////


					//Fill prex cut histgrams
					for (int hist_couplet_index=0; hist_couplet_index < hist_array_in.size(); hist_couplet_index++){
						//unpack
						def hist_couplet = hist_array_in[hist_couplet_index]
						def hist_params = hist_couplet[0]
						def hist_mini_array = hist_couplet[1]	
						if (hist_params.get("prex_fill") == "yes"){	


							def all_index = 5
							def fd_index = 4
							def cd_index = 3
							def variable_map = variable_map_excuts
												
							def fillvars = [variable_map.get(hist_params.get("fill_x")),]	
							if(hist_params.get("num_bins_z") > 0){ fillvars.add(variable_map.get(hist_params.get("fill_z")))	}

							//println(hist_params.get("fill_z"))
							//Fill histos
							hist_mini_array[all_index].fill(fillvars) //Fill "All" histogram
							
							if (proton_location == 'FD'){ hist_mini_array[fd_index].fill(fillvars)	} //Fill FD
							if (proton_location == 'CD'){hist_mini_array[cd_index].fill(fillvars)	} //Fill CD

													
							//Repack
							hist_array_in[hist_couplet_index] = [hist_params,hist_mini_array]
						}
					}
					
				
					def dvep_array = [particleX, particleGammaGammaPair, wvec, qvec, electron_sector, pho1sec, pho2sec]
					def is_DVEP_event = DVEPCutter.cutDVEP(dvep_array,cuts_array,event_number)


					if (4054024  == event_number){
						println("ON STAGE FOUR RIGHT EVENT NUMBER $event_number")
						println(is_DVEP_event)						
					}


					if(!(ispi0 && is_DVEP_event)) { continue}

					//IF WE HAVE MADE IT THIS FAR, WE NOW HAVE A DVEP event0!!!!!!!!


					/////////////////////////////////////////////
					// FOURTH HISTOGRAM FILL SPOT //////////////
					/////////////////////////////////////////////////


					//Fill excut histgrams
					for (int hist_couplet_index=0; hist_couplet_index < hist_array_in.size(); hist_couplet_index++){
						//unpack
						def all_index = 2
						def fd_index = 1
						def cd_index = 0
						def variable_map = variable_map_excuts
						def hist_couplet = hist_array_in[hist_couplet_index]
						def hist_params = hist_couplet[0]
						def hist_mini_array = hist_couplet[1]							
						def fillvars = [variable_map.get(hist_params.get("fill_x")),]	
						if(hist_params.get("num_bins_z") > 0){ fillvars.add(variable_map.get(hist_params.get("fill_z")))	}


						if (hist_params.get("ex_fill") == "yes"){	

							if (hist_params.get("bins_xb") == "yes"){
								if (hist_params.get("bins_q2") == "yes"){
									if (hist_params.get("bins_t") == "yes"){
										hist_mini_array[all_index][title_xbq2t].fill(fillvars)
										if (proton_location == 'FD'){ hist_mini_array[fd_index][title_xbq2t].fill(fillvars)	} //Fill FD
										if (proton_location == 'CD'){hist_mini_array[cd_index][title_xbq2t].fill(fillvars)	} //Fill CD
									}
									if (hist_params.get("bins_t") == "no"){
										hist_mini_array[all_index][title_xbq2].fill(fillvars)
										if (proton_location == 'FD'){ hist_mini_array[fd_index][title_xbq2].fill(fillvars)	} //Fill FD
										if (proton_location == 'CD'){hist_mini_array[cd_index][title_xbq2].fill(fillvars)	} //Fill CD
									}
								}
							}
							else{
								//Fill histos
								hist_mini_array[all_index].fill(fillvars) //Fill "All" histogram
								if (proton_location == 'FD'){ hist_mini_array[fd_index].fill(fillvars)	} //Fill FD
								if (proton_location == 'CD'){hist_mini_array[cd_index].fill(fillvars)	} //Fill CD
							}	
							//Repack
							hist_array_in[hist_couplet_index] = [hist_params,hist_mini_array]

						}					
					
					}

					if (13181 == event_number){
						println("ON RIGHT EVENT NUMBER $event_number")
						println(electrons_in_event)
						println(protons_in_event)
						println(good_photons_in_event)
						
					}
					

					["particleProtonTheta":particleProtonTheta,"particleElectronTheta":particleElectronTheta,
					"particleProtonPhi":particleProtonPhi,"particleElectronPhi":particleElectronPhi,
					"particlePionEnergy":particleGammaGammaPair.e(),"particlePionMass":particleGammaGammaPairmass*1000,
					"particleXEnergy":particleX.e(),"particleXMassSquared":particleX.mass2(),
					"momentumDiffX":diff_between_X_and_GG.px().abs()*1000,"momentumDiffY":diff_between_X_and_GG.py().abs()*1000,
					"missingEnergyDifference":dmisse0,
					"thetaXPi":thetaXPi,"t_momentum":t_momentum,"t_momentum_recon":t_momentum_recon,
					"q2":-qvec.mass2(),"xb":xBjorken, "LeptHadAngle":LeptHadAngle,"LeptHadAngle_pos":LeptHadAngle_pos, "LeptHadAngle_neg":LeptHadAngle_neg, "w_vector":wvec.mass(),
					"particleProtonMass":particleProton.mass(),
					"particle0Energy":particle0.e(),"particle0MassSquared":particle0.mass2(),
					 "xbbad":xBjorkenBad,"number_photons_good":good_photons_in_event.size(),"number_photons_bad":good_photons_in_event.size(),
					"number_protons":protons_in_event.size(),"electron_sector":electron_sector,
					"pho1sec":pho1sec, "pho2sec":pho2sec
					]

					//println("PION MASS IS")
					//println(particleGammaGammaPairmass)

					
					def return_array_vals = [event_number,i_helicity,xBjorken,-qvec.mass2(),t_momentum,LeptHadAngle, wvec.mass(), thetaXPi, diff_between_X_and_GG.px(), diff_between_X_and_GG.py(),particleX.mass2(),dmisse0,particleGammaGammaPairmass]
					return_array = return_array_vals

					dvpp_event += 1

					if (proton_location == 'FD'){ fd_dvpp_event += 1	}
					if (proton_location == 'CD'){ cd_dvpp_event += 1	}

					
					
					


					// println("FOUND DVPP EVENT, PROTON LOC IS:")
					// println(proton_location)
					// println(cd_dvpp_event)
					// println(dvpp_event)
					// println(event_number)

					
					//return [fcupBeamChargeMax, dvpp_event, fd_dvpp_event, cd_dvpp_event, fd_all_event, cd_all_event, hist_array_in,return_array]
				}
			}
		}

		//println("FINAL proton info")
		//println(bankParticle.getShort('status',indexProton))
		//println(fd_all_event)
		//println(cd_all_event)
		//println("END OF INFO")

		
		//println("DVEP EVENT NOT FOUND, RETURNING")
		return [fcupBeamChargeMax, dvpp_event, fd_dvpp_event, cd_dvpp_event, fd_all_event, cd_all_event, hist_array_in,return_array,0,num_electrons]
	}

}
