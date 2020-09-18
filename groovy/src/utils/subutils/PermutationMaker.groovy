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

class PermutationMaker {

	//Creates permutations from a list, e.g. [0,1,2] returns [[0,1],[0,2],[1,2]]

	static def makePermutations(numberArray) {

		def permutations = []
		def possible_comb = []

		if (numberArray.size() > 0){
			for (int element1 in 0..<(numberArray.size()-1)){
				for (int element2 in (element1+1)..<numberArray.size()){
					possible_comb = [numberArray[element1], numberArray[element2]]
					permutations.add(possible_comb)
				}
			}
		}			

		return permutations
	}

}