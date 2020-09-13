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

class EventProcessor {

	def dvpp_event = 0

	def processEvent(event,hxB,fcc_final) {

		def beam = LorentzVector.withPID(11,0,0,10.6)
		def target = LorentzVector.withPID(2212,0,0,0)

		def banknames = ['REC::Event','REC::Particle','REC::Cherenkov','REC::Calorimeter','REC::Traj','REC::Track','REC::Scintillator']

		if(banknames.every{event.hasBank(it)}) {
			def (evb,partb,cc,ec,traj,trck,scib) = banknames.collect{event.getBank(it)}
			def banks = [cc:cc,ec:ec,part:partb,traj:traj,trck:trck]
			def ihel = evb.getByte('helicity',0)
			//printerUtil.printer("ihel is "+ihel,0)

			def fcupBeamCharge = evb.getFloat('beamCharge',0)

			if(fcupBeamCharge > fcc_final){
				//println("fcup charge increashing to "+ fcupBeamCharge)
				fcc_final = fcupBeamCharge
					}

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
			//printerUtil.printer("index_of_electrons_and_protons "+index_of_electrons_and_protons,0)

			def index_of_pions = (0..<partb.rows()-1).findAll{partb.getInt('pid',it)==22 && partb.getShort('status',it)>=2000}
				.findAll{ig1->'xyz'.collect{partb.getFloat("p$it",ig1)**2}.sum()>0.16}
				.collectMany{ig1->
				(ig1+1..<partb.rows()).findAll{partb.getInt('pid',it)==22 && partb.getShort('status',it)>=2000}
				.findAll{ig2->'xyz'.collect{partb.getFloat("p$it",ig2)**2}.sum()>0.16}
				.collect{ig2->[ig1,ig2]}
			}
			///printerUtil.printer("index of pions is " + index_of_pions,0)

			def isep0s = index_of_electrons_and_protons.findAll{iele,ipro->
				def ele = LorentzVector.withPID(11,*['px','py','pz'].collect{partb.getFloat(it,iele)})
				def pro = LorentzVector.withPID(2212,*['px','py','pz'].collect{partb.getFloat(it,ipro)})
				//printerUtil.printer("first electron is"+ele,0)

				if(event.hasBank("MC::Particle")) {
					printer("Event has MC Particle bank!",1)
					def mcb = event.getBank("MC::Particle")
					def mfac = (partb.getShort('status',ipro)/1000).toInteger()==2 ? 3.2 : 2.5

					def profac = 0.9

					//mfac=1
					profac = 1.0

					ele = LorentzVector.withPID(11,*['px','py','pz'].collect{mcb.getFloat(it,0) + (partb.getFloat(it,iele)-mcb.getFloat(it,0))*mfac})
					pro = LorentzVector.withPID(2212,*['px','py','pz'].collect{profac*(mcb.getFloat(it,1) + (partb.getFloat(it,ipro)-mcb.getFloat(it,1))*mfac)})
					//printerUtil.printer("second electron is"+ele)
					def evec = new Vector3()
					evec.setMagThetaPhi(ele.p(), ele.theta(), ele.phi())
					def pvec = new Vector3()
					pvec.setMagThetaPhi(pro.p(), pro.theta(), pro.phi())
				}

				def eletheta = Math.toDegrees(ele.theta())
				def protheta = Math.toDegrees(pro.theta())
				if(protheta<0) protheta+=360



				def wvec = beam+target-ele
				def qvec = beam-ele
				def epx = beam+target-ele-pro
				//def t_sqrt = pro - target //t = (p'-p)^2
				//def t_MomTran = t_sqrt.vect().dot(t_sqrt.vect())

				//printerUtil.printer("epx mass squared is:${epx.mass2()}",0)
				def xBjorken = -qvec.mass2()/(2*pro.vect().dot(qvec.vect()))
				//printerUtil.printer("adding XB to hist "+index_of_electrons_and_protons,0)
				hxB.fill(xBjorken)

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
							//printerUtil.printer("Q2 is ${-qvec.mass2()} = $q2Round and xB is $xBjorken = $xBRound",0)

							//if (q2Round < 0.4){
								//print("Q2 is low at $q2round",2)
							//}

							def title = "${((xBRound-1)/xb_bins).round(2)} < xB < ${((xBRound)/xb_bins).round(2)}_ ${q2Round - 0.5} < q2 < ${q2Round+0.0}"
							//printer("Associated title is $title",2)

							//println(t_bins)
							def tRound = (Math.round(tt0*10)/10)
							def UltraTitle = "88"

							def TitleUltra = 0

							if(isep0){
								if(ispi0 && isep0 && dmisse0 && dpt0 && thetaXPi<2){


									TitleUltra = 1
									//if(TitleUltra==0){
								//		println("Ultra is 0, t is $tt0")
								//	}

									dvpp_event = 1
								}
							}
						}
						//return ispi0
					}
				}
				//return false
			}
		}
		return [fcc_final, 1, hxB]
	}

}