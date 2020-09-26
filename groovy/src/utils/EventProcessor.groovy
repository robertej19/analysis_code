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
import utils.subutils.PionCutter
import utils.subutils.DVEPCutter


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
	def processEvent(j,event,hist_array_in,fcupBeamChargeMax,cuts_array,binning_scheme) {
		//println("starting to process event")

		
		//Unfold histgrams
		

		//Define standard variables

		def binning_xb = binning_scheme[0]
		def binning_q2 = binning_scheme[1]
		def binning_t = binning_scheme[2]

		def dvpp_event = 0
		def fd_event = 0 
		def cd_event = 0


		def xb_bins = 10
		def beam = LorentzVector.withPID(11,0,0,10.6)
		def target = LorentzVector.withPID(2212,0,0,0)

		def hist_array_out = []



		def banknames = ['REC::Event','REC::Particle','REC::Cherenkov','REC::Calorimeter','REC::Traj','REC::Track','REC::Scintillator']


		// Leave event if not all banks are present
		if(!(banknames.every{event.hasBank(it)})) {
			//println("Not all bank events found, returning")
			return [fcupBeamChargeMax, dvpp_event, fd_event, cd_event, hist_array_in]
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


		if (electrons_in_event.size() > 1){
			println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX more than 1 electron in event: ")
			println(electrons_in_event)
		}


		//Get a list of "good" photons in the event
		def good_photons_in_event = ParticleGetter.getParticle(bankParticle,"photon")
		def bad_photons_in_event = ParticleGetter.getParticle(bankParticle,"photon_raw")

		//Create a set of all possible pairwise permutations of the photons (need 2 photons for pion)
		def photon_perms = PermutationMaker.makePermutations(good_photons_in_event)

		
		//Here, we loop over all pairs of [electron, proton] in index_of_electrons_and_protons. Most of the time there is only one set, 
		//Some of the tiem there are multiple pairs, e.g. [[0,1],[0,3]]
		for (int indexElectron in electrons_in_event){
			for (int indexProton in protons_in_event){ 
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
				def xBjorken = qsquared/(2*particleProton.vect().dot(qvec.vect()))
				//printerUtil.printer("adding XB to hist "+index_of_electrons_and_protons,0)
	
				def particleProtonPhi = Math.toDegrees(particleProton.phi())
				if(particleProtonPhi<0) particleProtonPhi+=360

				def particleElectronPhi = Math.toDegrees(particleElectron.phi())
				if(particleElectronPhi<0) particleElectronPhi+=360
				

			//This is currently unused, I think for handling sectors
				def esec = (0..<bankScintillator.rows()).find{bankScintillator.getShort('pindex',it)==indexElectron}?.with{bankScintillator.getByte('sector',it)}
				def psec = (0..<bankScintillator.rows()).find{bankScintillator.getShort('pindex',it)==indexProton}?.with{bankScintillator.getByte('sector',it)}
				if(psec==0) {
					psec = Math.floor(particleProtonPhi/60).toInteger() +2
					if(psec==7) psec=1
				}
			//Finishes comments

				def proton_location = (bankParticle.getShort('status',indexProton)/1000).toInteger()==2 ? 'FD':'CD' //This returns FD if proton in FD, CD if CD




				def t_momentum = -(particleProton-target).mass2() //This is the kinematic variable t

				
				def q2Round = Math.round((-qvec.mass2())*2+0.5)/2
				def xBRound = Math.round(xb_bins*xBjorken+0.5)
				def tRound = (Math.round(t_momentum*10)/10)
				

				def variable_map_nocuts = ["particleProtonTheta":particleProtonTheta,"particleElectronTheta":particleElectronTheta,
								"particleProtonPhi":particleProtonPhi,"particleElectronPhi":particleElectronPhi,
								"t_momentum":t_momentum,
								"q2":-qvec.mass2(),"xb":xBjorken, "w_vector":wvec.mass(),
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


				//Fill nocut histgrams
				for (int hist_couplet_index=0; hist_couplet_index < hist_array_in.size(); hist_couplet_index++){
					//unpack
					def hist_couplet = hist_array_in[hist_couplet_index]
					def hist_params = hist_couplet[0]
					def hist_mini_array = hist_couplet[1]	
					if (hist_params.get("ex_no_cuts_split") == "yes"){	

						def all_index = 8
						def fd_index = 7
						def cd_index = 6
						def variable_map = variable_map_nocuts
											
						def fillvars = [variable_map.get(hist_params.get("fill_x")),]	
						if(hist_params.get("num_bins_z") > 0){ fillvars.add(variable_map.get(hist_params.get("fill_z")))	}

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

					def particleGamma_1 = LorentzVector.withPID(22,*['px','py','pz'].collect{bankParticle.getFloat(it,indexParticleGamma_1)})
					def particleGamma_2 = LorentzVector.withPID(22,*['px','py','pz'].collect{bankParticle.getFloat(it,indexParticleGamma_2)})
					
					

					def particleGammaGammaPair = particleGamma_1+particleGamma_2
					def particleGammaGammaPairmass = particleGammaGammaPair.mass()
					

					//hist_pion_mass_nocuts.fill(particleGammaGammaPairmass*1000) //Report the mass in MeV


					def pion_cuts = [particleGamma_1,particleGamma_2,particleElectron]
					def ispi0 = PionCutter.cutPions(pion_cuts,cuts_array)

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


					def variable_map_excuts = ["particleProtonTheta":particleProtonTheta,"particleElectronTheta":particleElectronTheta,
					"particleProtonPhi":particleProtonPhi,"particleElectronPhi":particleElectronPhi,
					"particlePionEnergy":particleGammaGammaPair.e(),"particlePionMass":particleGammaGammaPairmass*1000,
					"particleXEnergy":particleX.e(),"particleXMass":particleX.mass(),
					"momentumDiffX":diff_between_X_and_GG.px().abs()*1000,"momentumDiffY":diff_between_X_and_GG.py().abs()*1000,
					"missingEnergyDifference":dmisse0,
					"thetaXPi":thetaXPi,"t_momentum":t_momentum,"t_momentum_recon":t_momentum_recon,
					"q2":-qvec.mass2(),"xb":xBjorken, "LeptHadAngle":LeptHadAngle,"w_vector":wvec.mass(),
					]




					//Fill prex cut histgrams
					for (int hist_couplet_index=0; hist_couplet_index < hist_array_in.size(); hist_couplet_index++){
						//unpack
						def hist_couplet = hist_array_in[hist_couplet_index]
						def hist_params = hist_couplet[0]
						def hist_mini_array = hist_couplet[1]	
						if (hist_params.get("prex_cuts") == "yes"){	


							def all_index = 5
							def fd_index = 4
							def cd_index = 3
							def variable_map = variable_map_excuts
												
							def fillvars = [variable_map.get(hist_params.get("fill_x")),]	
							if(hist_params.get("num_bins_z") > 0){ fillvars.add(variable_map.get(hist_params.get("fill_z")))	}

							//Fill histos
							hist_mini_array[all_index].fill(fillvars) //Fill "All" histogram
							if (proton_location == 'FD'){ hist_mini_array[fd_index].fill(fillvars)	} //Fill FD
							if (proton_location == 'CD'){hist_mini_array[cd_index].fill(fillvars)	} //Fill CD
													
							//Repack
							hist_array_in[hist_couplet_index] = [hist_params,hist_mini_array]
						}
					}
					
				
					def dvep_array = [particleX, particleGammaGammaPair, wvec, qvec]
					def is_DVEP_event = DVEPCutter.cutDVEP(dvep_array,cuts_array)



					if(!(ispi0 && is_DVEP_event)) { continue}

					//IF WE HAVE MADE IT THIS FAR, WE NOW HAVE A DVEP EVENT!!!!!!!!



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

					
					if (proton_location == 'FD'){ fd_event = 1	}
					if (proton_location == 'CD'){ cd_event = 1	}

					dvpp_event = 1
				}
			}
		}

		return [fcupBeamChargeMax, dvpp_event, fd_event, cd_event, hist_array_in]
	}

}
