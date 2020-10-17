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


class DVEPCutter {

	//def criteria_proton = bankParticle.getInt('pid',it)==2212


	//NEED Q2 GREATER THAN 1
	static def cutDVEP(particleArray,allCutParams) {


		def cutParams = allCutParams[0] //This is the set of cut for DVEP events



		def WMass_min = cutParams.get("WMass")
		def Q2_min = cutParams.get("Q2")
		def ThetaXPi_max = cutParams.get("ThetaDifference_X_Pi")
		def MomDiff_max = cutParams.get("MomentumDifference_X_Pi")
		def MissingMassSquared_max = cutParams.get("MissingMassSquared")
		def MissingEnergy_max =  cutParams.get("MissingEnergy")




		// Particle unpacking
		def particleX = particleArray[0]
		def pion = particleArray[1]
		def wvector = particleArray[2]
		def qvector = particleArray[3]


	
	
		def thetaXPi = particleX.vect().theta(pion.vect())
		def diff_between_X_and_GG = particleX - pion
		def dmisse0 = (particleX - pion).e()


		// Exclusive cuts:
		
		def WMass = wvector.mass2() > WMass_min
		def QSquared = -qvector.mass2() > Q2_min
		def ThetaXPi = thetaXPi < ThetaXPi_max
		def Dpt0 = diff_between_X_and_GG.px().abs() < MomDiff_max && diff_between_X_and_GG.py().abs() < MomDiff_max
		def Dmisse0 = dmisse0.abs() < MissingEnergy_max
		def MissingMassSquared = particleX.mass2().abs() < MissingMassSquared_max






		def bool_is_DVEP = 0
		if (MissingMassSquared && WMass && QSquared && ThetaXPi && Dpt0 && Dmisse0){
			bool_is_DVEP = 1
		}



		return bool_is_DVEP
	}

}