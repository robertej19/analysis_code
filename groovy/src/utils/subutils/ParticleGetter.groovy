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

class ParticleGetter {

	//def criteria_proton = bankParticle.getInt('pid',it)==2212


	//NEED Q2 GREATER THAN 1
	static def getParticle(bankParticle,particleType) {

		def particle_index = []

		for (int index in 0..<bankParticle.rows()){

			if(particleType == "electron"){
				if (bankParticle.getInt('pid',index)==11 && bankParticle.getShort('status',index)<0){
						particle_index.add(index)
				}
			}

			if(particleType == "proton"){
				if (bankParticle.getInt('pid',index)==2212){
					particle_index.add(index)
				}
			}

			if(particleType == "photon"){
				if (bankParticle.getInt('pid',index)==22 && bankParticle.getShort('status',index)>=2000){
					//println("index is: " + index)

					def px = bankParticle.getFloat("px",index)
					def py = bankParticle.getFloat("py",index)
					def pz = bankParticle.getFloat("pz",index)

					//println("px is: " + px)
					//println("py is: " + py)
					//println("pz is: " + pz)

					def p2 = px**2 + py**2 + pz**2

					//println("p squared is: " + p2)

					if (p2 > 0.16){
						particle_index.add(index)
					}
				}

			}

			if(particleType == "photon_raw"){
				if (bankParticle.getInt('pid',index)==22 && bankParticle.getShort('status',index)>=2000){	
					particle_index.add(index)
				}
			}





		}			

		return particle_index
	}

}