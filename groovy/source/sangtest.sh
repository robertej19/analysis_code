import org.jlab.io.hipo.HipoDataSource
import org.jlab.groot.data.H1F
import org.jlab.groot.data.H2F
import org.jlab.groot.data.TDirectory
import groovyx.gpars.GParsPool
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import sangbaek.draw_dcr
import sangbaek.dvcs
import sangbaek.dvcs_corr
import event.Event
import event.EventConverter
import my.Sugar

Sugar.enable()
/////////////////////////////////////////////////////////////////////

def outname = args[0].split('/')[-1]

def processors = [new dvcs(), new dvcs_corr()]

def evcount = new AtomicInteger()
def save = {
  processors.each{
    def out = new TDirectory()
    out.mkdir("/root")
    out.cd("/root")
    it.hists.each{out.writeDataSet(it.value)}
    def clasname = it.getClass().getSimpleName()
    out.writeFile("${clasname}.hipo")
  }
  println "event count: "+evcount.get()
  evcount.set(0)
}

def exe = Executors.newScheduledThreadPool(1)
exe.scheduleWithFixedDelay(save, 5, 30, TimeUnit.SECONDS)

GParsPool.withPool 12, {
  args.eachParallel{fname->
    def reader = new HipoDataSource()
    reader.open(fname)

    while(reader.hasEvent()) {
      evcount.getAndIncrement()
      def data_event = reader.getNextEvent()
      def event = EventConverter.convert(data_event)
      processors.each{it.processEvent(event)}
    }

    reader.close()
  }
q}

processors.each{if(it.metaClass.respondsTo(it, 'finish')) it.finish()}

exe.shutdown()
save()
