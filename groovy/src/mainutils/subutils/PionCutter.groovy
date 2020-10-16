package mainutils.subutils


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

class PionCutter {

	//def criteria_proton = bankParticle.getInt('pid',it)==2212


	//NEED Q2 GREATER THAN 1
	static def cutPions(particleArray,allCutParams) {

		def cutParams = allCutParams[1] //This is the set of cut for pions

		def photon1 = particleArray[0]
		def photon2 = particleArray[1]
		def electron = particleArray[2]

		def electron_sector =  particleArray[3]
		def photon_sector1 =  particleArray[4]
		def photon_sector2 =  particleArray[5]

		def bool_is_pion = 0

		def pion = photon1 + photon2
		def pion_mass = pion.mass()
		def pion_momentum = pion.p()

		def pi_mass_low = cutParams.get("pion_lower_mass")
		def pi_mass_high = cutParams.get("pion_upper_mass")
		def pi_mom_min = cutParams.get("pion_min_momentum")
		def pi_BH_angle_min = cutParams.get("photon_BH_angle")

		



		def cut_mass = pion_mass > pi_mass_low && pion_mass < pi_mass_high
		def cut_momentum = pion_momentum > pi_mom_min




		def cut_sector_pho1 = electron_sector !== photon_sector1
		def cut_sector_pho2 = electron_sector !== photon_sector2
		//def cut_BH1 = electron.vect().theta(photon1.vect()) > pi_BH_angle_min
		//def cut_BH2 = electron.vect().theta(photon2.vect()) > pi_BH_angle_min

		
		//if (cut_mass && cut_momentum && cut_BH1 && cut_BH2){
		if (cut_mass && cut_momentum && cut_sector_pho1 && cut_sector_pho2){
			//println(electron_sector)
			//println(photon_sector1)
			//println(photon_sector2)
			//println(cut_sector_pho1)
			//println(cut_sector_pho2)
			bool_is_pion = 1
		}


		return bool_is_pion
	}

}