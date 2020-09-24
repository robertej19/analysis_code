package utils.subutils


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
	static def cutDVEP(particleArray) {

		
		// Particle unpacking
		def particleX = particleArray[0]
		def pion = particleArray[1]
		def wvector = particleArray[2]
		def qvector = particleArray[3]


	
	
		def thetaXPi = particleX.vect().theta(pion.vect())
		def diff_between_X_and_GG = particleX - pion
		def dmisse0 = (particleX - pion).e()


		// Exclusive cuts:
		def MissingMassSquared = particleX.mass2()< 1
		def WMass = wvector.mass()>2
		def QSquared = -qvector.mass2() > 1
		def ThetaXPi = thetaXPi<2
		def Dpt0 = diff_between_X_and_GG.px().abs()<0.3 && diff_between_X_and_GG.py().abs()<0.3
		def Dmisse0 = dmisse0<1






		def bool_is_DVEP = 0
		if (MissingMassSquared && WMass && QSquared && ThetaXPi && Dpt0 && Dmisse0){
			bool_is_DVEP = 1
		}



		return bool_is_DVEP
	}

}