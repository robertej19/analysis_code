package pid.electron

class Electron {
  static def findElectron = { event ->
    def pbank = event.getBank("REC::Particle")
    return (0..<pbank.rows())
      .find{pbank.getInt('pid',it)==11 && pbank.getShort('status',it)<0}
  }
}
