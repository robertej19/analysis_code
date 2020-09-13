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


class ScreenUpdater {

    def Mil = 1000000

    def UpdateScreen(FileStartTime,CurrentCounter,CountRate,NumTotalCounts,filename){
        if(CurrentCounter % CountRate == 0){
            def runtime = new Date()
            def TimeElapsed = (runtime.getTime() - FileStartTime)/1000/60
            def CountsLeft = NumTotalCounts-CurrentCounter
            def TimeLeft = TimeElapsed/CurrentCounter*CountsLeft
            def Rate = CurrentCounter/TimeElapsed/1000/60
            def uTS = Math.round(TimeLeft*60+runtime.getTime()/1000)
            def eta = Date.from(Instant.ofEpochSecond(uTS)).format('HH:mm:ss')


            //printerUtil.printer("Total running time in minutes is: ${TimeElapsed.round(2)}",2)
            println("\n \n \n${(CurrentCounter/Mil).round(2)}M events have been processed from ${filename}, ${(CountsLeft/Mil).round(2)}M events remain")
            println("Processing Rate is ${Rate.round(1)} kHz")

            if(TimeLeft > 1){
                println("Anticipated file finish time is $eta, ${TimeLeft.round(2)} minutes left")
            }
            else{
                println("Anticipated file finish time is $eta, ${(TimeLeft*60).round(2)} seconds left")
            }
            return TimeLeft
        }

    }
}