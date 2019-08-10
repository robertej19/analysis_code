import org.jlab.io.hipo.HipoDataSource
import org.jlab.io.hipo.HipoDataSync
import org.jlab.detector.base.DetectorType
import org.jlab.clas.physics.Particle
import org.jlab.clas.physics.Vector3
import org.jlab.groot.data.H1F
import org.jlab.groot.data.H2F
import org.jlab.groot.data.TDirectory


def hmm2phi = new H2F('hmm2phi', 'mm2 vs #phi',100,0,200, 100,0,10)

def reader = new HipoDataSource()
reader.open(args[0])

def target = new Particle(2212, 0,0,0)

while(reader.hasEvent()) {
  def event = reader.getNextEvent()
  if (event.hasBank("REC::Particle") && event.hasBank("REC::Calorimeter")) {
    def partbank = event.getBank("REC::Particle")
    def calbank = event.getBank("REC::Calorimeter")

    def eleindex = (0..<partbank.rows()).find{partbank.getInt('pid',it)==11 && partbank.getShort('status',it)<0}
    if(eleindex!=null) {
      def proindices = (0..<partbank.rows()).findAll{partbank.getInt('pid',it)==2212}

      def secs = [calbank.getShort('pindex')*.toInteger(), calbank.getByte('sector')].transpose().collectEntries()

      //find proton with maximum energy
      def getmom2 = {index -> (new Vector3(*['px', 'py', 'pz'].collect{partbank.getFloat(it,index)})).mag2()}
      def proindex = proindices.max{a,b -> getmom2(a)<=>getmom2(b)}

      if(proindex!=null) {
        def electron = new Particle(11, *['px', 'py', 'pz'].collect{partbank.getFloat(it, eleindex)})
        def proton = new Particle(2212, *['px', 'py', 'pz'].collect{partbank.getFloat(it, proindex)})

        def eX = new Particle(11, 0,0,7.546)
        eX.combine(target, 1)
        eX.combine(electron,-1)

        def mm2 = eX.mass2()

        //angle between electron and proton planes (should be close to 180)
        def zaxis = new Vector3(0,0,1)
        def enorm = electron.vector().vect().cross(zaxis)
        def pnorm = proton.vector().vect().cross(zaxis)
        def phi = enorm.theta(pnorm)

        hmm2phi.fill(phi, mm2)
      }
    }
  }
}

reader.close()

def out = new TDirectory()
out.mkdir('/hists')
out.cd('/hists')
out.addDataSet(hmm2phi.projectionX())
out.addDataSet(hmm2phi.projectionY())
out.addDataSet(hmm2phi)
out.writeFile('elastic_out.hipo')
