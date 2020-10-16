package mainutils.event
import org.jlab.detector.base.DetectorType
import org.jlab.io.hipo.HipoDataEvent
import mainutils.event.Event
import mainutils.event.DCHit
import mainutils.event.FtofHit
import mainutils.event.ECHit

class EventConverter {

    static def convert(HipoDataEvent dataEvent){
        def event = new Event()
        convertScalars(dataEvent,event)
        convertPart(dataEvent,event)
	convertCovMat(dataEvent,event)
        convertCherenkov(dataEvent,event)
        convertCalorimeter(dataEvent,event)
        convertTimeOfFlight(dataEvent,event)
        convertDriftChamber(dataEvent,event)
        convertMC(dataEvent,event)
        return event
    }

    static def convertScalars(HipoDataEvent dataEvent, Event event){
        if (dataEvent.hasBank("REC::Event")){
            def evt = dataEvent.getBank("REC::Event")
            event.start_time = evt.getFloat("startTime",0)
            event.rf_time = evt.getFloat("RFTime",0)
            event.helicity = evt.getByte("helicity",0)
        }
        if (dataEvent.hasBank("RUN::config")){
            def conf = dataEvent.getBank("RUN::config")
            event.event_number = conf.getInt("event",0)
            event.run_number = conf.getInt("run",0)
        }
    }

    static def convertPart(HipoDataEvent dataEvent, Event event){
        if (dataEvent.hasBank("REC::Particle")){
            def part = dataEvent.getBank("REC::Particle")
            event.npart = part.rows()
            (0 ..< part.rows()).each{ index ->
                event.px.put(index, part.getFloat('px', index))
                event.py.put(index, part.getFloat('py', index))
                event.pz.put(index, part.getFloat('pz', index))
                event.p.put(index,
                        Math.sqrt(event.px.get(index)**2 + event.py.get(index)**2 + event.pz.get(index)**2))
                event.vx.put(index, part.getFloat('vx', index))
                event.vy.put(index, part.getFloat('vy', index))
                event.vz.put(index, part.getFloat('vz', index))
                event.vt.put(index, part.getFloat('vt', index))
                event.beta.put(index, part.getFloat('beta', index))
                event.chi2pid.put(index, part.getFloat('chi2pid', index))
                event.status.put(index, part.getShort('status', index))
                event.charge.put(index, part.getByte('charge', index))
                event.pid.put(index, part.getInt('pid', index))
            }
        } else {
            event.npart = 0
        }
    }

    static def convertCovMat(HipoDataEvent dataEvent, Event event){
        if (dataEvent.hasBank("REC::CovMat")){
            def cov = dataEvent.getBank("REC::CovMat")
            (0 ..< cov.rows()).each{ index ->
                def pindex = cov.getShort('pindex',index).toInteger()		
		def cmatrix = new CovarianceMatrix()
		cmatrix.c11 = cov.getFloat('C11',index)
		cmatrix.c12 = cov.getFloat('C12',index)
		cmatrix.c13 = cov.getFloat('C13',index)
		cmatrix.c14 = cov.getFloat('C14',index)
		cmatrix.c15 = cov.getFloat('C15',index)
		cmatrix.c22 = cov.getFloat('C22',index)
		cmatrix.c23 = cov.getFloat('C23',index)
		cmatrix.c24 = cov.getFloat('C24',index)
		cmatrix.c25 = cov.getFloat('C25',index)
		cmatrix.c33 = cov.getFloat('C33',index)
		cmatrix.c34 = cov.getFloat('C34',index)
		cmatrix.c35 = cov.getFloat('C35',index)
		cmatrix.c44 = cov.getFloat('C44',index)
		cmatrix.c45 = cov.getFloat('C45',index)
		cmatrix.c55 = cov.getFloat('C55',index)
		event.covariance_status.add(pindex)
		event.cov_mat.put(pindex, cmatrix)
            }
        }
    }

    static def convertCherenkov(HipoDataEvent dataEvent, Event event){
        if (dataEvent.hasBank("REC::Cherenkov")){
            def cher = dataEvent.getBank("REC::Cherenkov")
            (0 ..< cher.rows()).each{ index ->
                def pindex = cher.getShort('pindex',index).toInteger()
                def detector = cher.getByte('detector', index)
                if (detector == DetectorType.HTCC.getDetectorId()){
                    event.cherenkov_status.add(pindex)
                    event.nphe.put(pindex, cher.getFloat('nphe', index))
                    event.cherenkov_sector.put(pindex, cher.getByte('sector', index))
                    event.cherenkov_time.put(pindex, cher.getFloat('time', index))
                    event.cherenkov_path.put(pindex, cher.getFloat('path', index))
                }
            }
        }
    }

    // All of the variable types need to be fixed and corrected
    // in the definitions and in the getInt getShort calls.
    static def convertCalorimeter(HipoDataEvent dataEvent, Event event){
        if (dataEvent.hasBank("REC::Calorimeter")){
            def cal = dataEvent.getBank("REC::Calorimeter")
            (0 ..< cal.rows()).each{ index ->
                def pindex = cal.getShort('pindex', index).toInteger()
                def layer = cal.getByte('layer', index)
                def hit = new ECHit(
                        x: cal.getFloat('x', index),
                        y: cal.getFloat('y', index),
                        z: cal.getFloat('z', index),
                        layer : cal.getByte('layer', index)
                )

                if (layer == 4){
                    event.ecal_inner_status.add(pindex)
                    event.ecal_inner_energy.put(pindex, cal.getFloat('energy', index))
                    event.ecal_inner_time.put(pindex, cal.getFloat('time', index))
                    event.ecal_inner_path.put(pindex, cal.getFloat('path', index))
                    event.ecal_inner_sector.put(pindex, cal.getByte('sector', index))
                    event.ecal_inner_u.put(pindex, cal.getFloat('lu', index))
                    event.ecal_inner_v.put(pindex, cal.getFloat('lv', index))
                    event.ecal_inner_w.put(pindex, cal.getFloat('lw', index))
                    event.ecal_inner.put(pindex, hit)
                }

                else if (layer == 7){
                    event.ecal_outer_status.add(pindex)
                    event.ecal_outer_energy.put(pindex, cal.getFloat('energy', index))
                    event.ecal_outer_time.put(pindex, cal.getFloat('time', index))
                    event.ecal_outer_path.put(pindex, cal.getFloat('path', index))
                    event.ecal_outer_sector.put(pindex, cal.getByte('sector', index))
                    event.ecal_outer_u.put(pindex, cal.getFloat('lu', index))
                    event.ecal_outer_v.put(pindex, cal.getFloat('lv', index))
                    event.ecal_outer_w.put(pindex, cal.getFloat('lw', index))
                    event.ecal_outer.put(pindex, hit)
                }

                else if (layer == 1){
                    event.pcal_status.add(pindex)
                    event.pcal_energy.put(pindex, cal.getFloat('energy', index))
                    event.pcal_time.put(pindex, cal.getFloat('time', index))
                    event.pcal_path.put(pindex, cal.getFloat('path', index))
                    event.pcal_sector.put(pindex, cal.getByte('sector', index))
                    event.pcal_u.put(pindex, cal.getFloat('lu', index))
                    event.pcal_v.put(pindex, cal.getFloat('lv', index))
                    event.pcal_w.put(pindex, cal.getFloat('lw', index))
                    event.pcal.put(pindex, hit)
                }
            }
        }
    }

    static def convertTimeOfFlight(HipoDataEvent dataEvent, Event event){
        if (dataEvent.hasBank("REC::Scintillator")){
            def tof = dataEvent.getBank("REC::Scintillator")
            (0 ..< tof.rows()).each{ index ->
                def pindex = tof.getShort('pindex', index).toInteger()
                def detector = tof.getByte('detector', index)

                // Add some logic to determine the paddle and the kind of
                // detector which was hit.
                if (detector == DetectorType.FTOF.getDetectorId()){

                    /* Outdated by FtofHit
                    event.tof_sector.put(pindex, tof.getByte('sector', index))
                    event.tof_layer.put(pindex, tof.getByte('layer', index))

                    // Check this for accuracy.  I'm not sure on paddle here.
                    event.tof_paddle.put(pindex, tof.getShort('component', index))
                    event.tof_time.put(pindex, tof.getFloat('time', index))
                    event.tof_path.put(pindex, tof.getFloat('path', index))
                    event.tof_energy.put(pindex, tof.getFloat('energy', index))
                    */

                    def hit = new FtofHit(
                            sector: tof.getByte('sector', index),
                            layer: tof.getByte('layer', index),
                            paddle: tof.getShort('component', index),
                            time: tof.getFloat('time', index),
                            energy: tof.getFloat('energy', index),
                            path: tof.getFloat('path', index)
                    )
                    if (event.tof_status.contains(pindex)){
                        event.tof.get(pindex).add(hit)
                    }
                    else {
                        event.tof_status.add(pindex)
                        event.tof.put(pindex, new ArrayList<FtofHit>())
                        event.tof.get(pindex).add(hit)
                    }
                }

                else if (detector == DetectorType.CTOF.getDetectorId()){
                    event.ctof_status.add(pindex)
                    event.ctof_sector.put(pindex, tof.getByte('sector', index))
                    event.ctof_layer.put(pindex, tof.getByte('layer', index))
                    event.ctof_component.put(pindex, tof.getShort('component', index))
                    event.ctof_time.put(pindex, tof.getFloat("time", index))
                    event.ctof_path.put(pindex, tof.getFloat("path", index))
                    event.ctof_energy.put(pindex, tof.getFloat("energy", index))
                }

            }
        }
    }

    static def convertDriftChamber(HipoDataEvent dataEvent, Event event){

        if (dataEvent.hasBank("REC::Traj")){
            def traj = dataEvent.getBank("REC::Traj")

            (0 ..< traj.rows()).findAll{index->
                def detector = traj.getByte('detector', index)
                detector == DetectorType.DC.getDetectorId()
            }.collect{ index ->
                [ pindex : traj.getShort('pindex', index).toInteger(),
                  data : new DCHit(
                          x : traj.getFloat('x', index),
                          y : traj.getFloat('y', index),
                          z : traj.getFloat('z', index),
                          cx : traj.getFloat('cx', index),
                          cy : traj.getFloat('cy', index),
                          cz : traj.getFloat('cz', index),
                          layer : traj.getByte('layer', index)
                  )
                ]
            }.groupBy{ entry -> entry.pindex }.each{ pindex, data ->
                def hits = data.collect{it.data}
                hits.each{ hit ->

                    // We have a region 1 hit
                    if (hit.layer < 13){
                        if (event.dc1_status.contains(pindex)){
                            event.dc1.get(pindex).add(hit)
                        }
                        else {
                            event.dc1_status.add(pindex)
                            event.dc1.put(pindex, new ArrayList<DCHit>())
                            event.dc1.get(pindex).add(hit)
                        }
                    }
                    else if (hit.layer >= 13 && hit.layer < 25){
                        if (event.dc2_status.contains(pindex)){
                            event.dc2.get(pindex).add(hit)
                        }
                        else {
                            event.dc2_status.add(pindex)
                            event.dc2.put(pindex, new ArrayList<DCHit>())
                            event.dc2.get(pindex).add(hit)
                        }
                    }
                    else if (hit.layer >= 25){
                        if (event.dc3_status.contains(pindex)){
                            event.dc3.get(pindex).add(hit)
                        }
                        else {
                            event.dc3_status.add(pindex)
                            event.dc3.put(pindex, new ArrayList<DCHit>())
                            event.dc3.get(pindex).add(hit)
                        }
                    }
                }
            }
        }

        if (dataEvent.hasBank("REC::Track")){
            def track = dataEvent.getBank("REC::Track")
            (0 ..< track.rows()).each{ index ->
                def pindex = track.getShort('pindex', index).toInteger()
                def detector = track.getByte('detector', index)
                if (detector == DetectorType.DC.getDetectorId()){
                    event.dc_sector.put(pindex, track.getByte('sector', index))
                    event.dc_chi2.put(pindex, track.getFloat('chi2', index))
                    event.dc_ndf.put(pindex, track.getShort('NDF', index))
                }

                else if (detector == DetectorType.CVT.getDetectorId()){
                    event.cvt_status.add(pindex)
                    event.cvt_chi2.put(pindex, track.getFloat('chi2', index))
                    event.cvt_ndf.put(pindex, track.getShort('NDF', index))
                }

            }
        }
    }

    static def convertMC(HipoDataEvent dataEvent, Event event){
        if (dataEvent.hasBank("MC::Particle")){
            def mc = dataEvent.getBank("MC::Particle")
            event.mc_status = true
            event.mc_npart = mc.rows()

            (0 ..< mc.rows()).each{ index ->
                event.mc_pid.put(index, mc.getInt('pid', index))
                event.mc_px.put(index, mc.getFloat('px', index))
                event.mc_py.put(index, mc.getFloat('py', index))
                event.mc_pz.put(index, mc.getFloat('pz', index))
                event.mc_p.put(index,
                        Math.sqrt(mc.getFloat('px',index)**2 + mc.getFloat('py',index)**2 + mc.getFloat('pz',index)**2))
                event.mc_vx.put(index, mc.getFloat('vx', index))
                event.mc_vy.put(index, mc.getFloat('vy', index))
                event.mc_vz.put(index, mc.getFloat('vz', index))
                event.mc_vt.put(index, mc.getFloat('vt', index))
            }

        } else {
            event.mc_status = false
            event.mc_npart = 0
        }
    }
}
