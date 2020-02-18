#!/usr/bin/groovy

import org.jlab.detector.base.DetectorType
import org.jlab.clas.physics.LorentzVector
import org.jlab.clas.physics.Vector3
import org.jlab.groot.data.H1F
import org.jlab.groot.data.H2F
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.nio.ByteBuffer


class eppi0_mon {
  def hists = new ConcurrentHashMap()

  def beam = LorentzVector.withPID(11,0,0,10.6)
  def target = LorentzVector.withPID(2212,0,0,0)

  def hw = {new H1F("$it","$it",200,0,5)}
  def hq2 = {new H1F("$it","$it",200,0,10)}
  def hfi = {new H1F("$it","$it",200,-360,360)}

  def hggpi0mom = {new H2F("$it","$it",200,0,10, 200,0.05,0.3)}
  def hgggmom = {new H2F("$it","$it",200,0,8, 200,0.05,0.3)}
  def hgg = {new H1F("$it","$it",200,0.05,0.3)}

  def hepx0 = {new H1F("$it","$it",200,-1,2)}
  def hepx = {new H1F("$it","$it",200,-0.7,0.7)}
  def hepxmom = {new H2F("$it","$it",200,0,4,200,-0.7,0.7)}
  def hepxth = {new H2F("$it","$it",300,0,60,200,-0.7,0.7)}

  def hpmom = {new H2F("$it","$it",200,0,4,400,-1,1)}
  def hpth = {new H2F("$it","$it",300,0,60,700,-1,1)}

  def hthmom = {new H2F("$it","$it",200,0,4,400,-70,70)}
  def hthth = {new H2F("$it","$it",300,0,60,700,-70,70)}

  def heggx = {new H1F("$it","$it",200,0,2)}
  def hmisse0 = {new H1F("$it","$it",200,-1,2)}
  def hmisse = {new H1F("$it","$it",200,-0.7,0.9)}
  def hmissp = {new H1F("$it","$it",200,-2,2)}
  def hth = {new H1F("$it","$it",200,0,15)}
  def hcombs = {new H1F("$it","$it",9,1,10)}

  def hmom = {new H1F("$it","$it",200,0,10)}
  def htheta = {new H1F("$it","$it",500,0,50)}

  def hepxtheta = {new H2F("$it","$it",200,-0.5,0.49,200,0,15)}
  def hepxmisse = {new H2F("$it","$it",200,-0.5,0.5,200,-1,1)}
  def hmissetheta = {new H2F("$it","$it",200,-1,1,200,0,15)}



  def banknames = ['REC::Event','REC::Particle','REC::Cherenkov','REC::Calorimeter','REC::Traj','REC::Track','REC::Scintillator']
  def processEvent(event) {
    if(banknames.every{event.hasBank(it)}) {
      def (evb,partb,cc,ec,traj,trck,scib) = banknames.collect{event.getBank(it)}
      def banks = [cc:cc,ec:ec,part:partb,traj:traj,trck:trck]
      def ihel = evb.getByte('helicity',0)

      def ieps = (0..<partb.rows()).findAll{partb.getInt('pid',it)==11 && partb.getShort('status',it)<0}
        .collectMany{iele->
          (0..<partb.rows()).findAll{partb.getInt('pid',it)==2212}.collect{ipro->[iele,ipro]}
        }

      def ipi0s = (0..<partb.rows()-1).findAll{partb.getInt('pid',it)==22 && partb.getShort('status',it)>=2000}
        .findAll{ig1->'xyz'.collect{partb.getFloat("p$it",ig1)**2}.sum()>0.16}
        .collectMany{ig1->
          (ig1+1..<partb.rows()).findAll{partb.getInt('pid',it)==22 && partb.getShort('status',it)>=2000}
            .findAll{ig2->'xyz'.collect{partb.getFloat("p$it",ig2)**2}.sum()>0.16}
            .collect{ig2->[ig1,ig2]}
        }

      def isep0s = ieps.findAll{iele,ipro->
        def ele = LorentzVector.withPID(11,*['px','py','pz'].collect{partb.getFloat(it,iele)})
        def pro = LorentzVector.withPID(2212,*['px','py','pz'].collect{partb.getFloat(it,ipro)})

        if(event.hasBank("MC::Particle")) {
          def mcb = event.getBank("MC::Particle")
          def mfac = (partb.getShort('status',ipro)/1000).toInteger()==2 ? 3.2 : 2.5

          def profac = 0.9

          //mfac=1
          profac = 1.0

          ele = LorentzVector.withPID(11,*['px','py','pz'].collect{mcb.getFloat(it,0) + (partb.getFloat(it,iele)-mcb.getFloat(it,0))*mfac})
          pro = LorentzVector.withPID(2212,*['px','py','pz'].collect{profac*(mcb.getFloat(it,1) + (partb.getFloat(it,ipro)-mcb.getFloat(it,1))*mfac)})

          def evec = new Vector3()
          evec.setMagThetaPhi(ele.p(), ele.theta(), ele.phi())
          def pvec = new Vector3()
          pvec.setMagThetaPhi(pro.p(), pro.theta(), pro.phi())
        }

        def wvec = beam+target-ele
        def qvec = beam-ele
        def epx = beam+target-ele-pro


        def pdet = (partb.getShort('status',ipro)/1000).toInteger()==2 ? 'FD':'CD'

        def profi = Math.toDegrees(pro.phi())
        if(profi<0) profi+=360

        def esec = (0..<scib.rows()).find{scib.getShort('pindex',it)==iele}?.with{scib.getByte('sector',it)}
        def psec = (0..<scib.rows()).find{scib.getShort('pindex',it)==ipro}?.with{scib.getByte('sector',it)}
        if(psec==0) {
          psec = Math.floor(profi/60).toInteger() +2
          if(psec==7) psec=1
        }

        def isep0 = epx.mass2()<1 && wvec.mass()>2

        def pi0s = ipi0s.collect{ig1,ig2->
          def g1 = LorentzVector.withPID(22,*['px','py','pz'].collect{partb.getFloat(it,ig1)})
          def g2 = LorentzVector.withPID(22,*['px','py','pz'].collect{partb.getFloat(it,ig2)})
          if(ele.vect().theta(g1.vect())>8 && ele.vect().theta(g2.vect())>8) {
            def gg = g1+g2
            def ggmass = gg.mass()
            def ispi0 = ggmass<0.2 && ggmass>0.07// && gg.p()>1.5

            if(ispi0) {
              def epggx = epx-gg
              def thetaXPi = epx.vect().theta(gg.vect())

              def dpt0 = epggx.px().abs()<0.3 && epggx.py().abs()<0.3
              def dmisse0 = epggx.e()<1

              def tt0 = -(pro-target).mass2()
              def procalc = beam+target-ele-gg
              def tt1 = -(procalc-target).mass2()

              [['gg/',ispi0],['gg/ep0/',ispi0 && isep0],
               ['gg/ep0/theta.gt.2/',ispi0 && isep0 && thetaXPi>2],
               ['gg/ep0/theta.lt.2/',ispi0 && isep0 && thetaXPi<2],
               ['gg/ep0/theta.lt.1/',ispi0 && isep0 && thetaXPi<1],
               ['gg/ep0/dpt/',ispi0 && isep0 && dpt0],
               ['gg/ep0/dmisse/',ispi0 && isep0 && dmisse0],
               ['gg/ep0/dmisse/dpt/',ispi0 && isep0 && dmisse0 && dpt0],
               ['',true]].findAll{it[1]}.each{prefix,test->
                 //hists.computeIfAbsent("${prefix}hdpx:$psec:$pdet",hmissp).fill(epggx.px())
                 //hists.computeIfAbsent("${prefix}hdpy:$psec:$pdet",hmissp).fill(epggx.py())
                 //hists.computeIfAbsent("${prefix}hdpz:$psec:$pdet",hmissp).fill(epggx.pz())
                 hists.computeIfAbsent("${prefix}hmisse:$psec:$pdet",hmisse0).fill(epggx.e())
                 hists.computeIfAbsent("${prefix}htheta:$psec:$pdet",htheta).fill(thetaXPi)
                 hists.computeIfAbsent("${prefix}hdt:$psec:$pdet",hepx).fill(tt0-tt1)
                 hists.computeIfAbsent("${prefix}hdtprop:$psec:$pdet",hepxmom).fill(pro.p(), tt0-tt1)
                 hists.computeIfAbsent("${prefix}hepx:$psec:$pdet",hepx0).fill(epx.mass2())
                 hists.computeIfAbsent("${prefix}hepxprop:$psec:$pdet",hepxmom).fill(pro.p(), epx.mass2())
                 hists.computeIfAbsent("${prefix}hpi0mom",hmom).fill(gg.p())
                 hists.computeIfAbsent("${prefix}hpi0mom:$pdet",hmom).fill(gg.p())
                 hists.computeIfAbsent("${prefix}helemom:$pdet",hmom).fill(ele.p())
                 hists.computeIfAbsent("${prefix}hpromom:$pdet",hmom).fill(pro.p())
                 hists.computeIfAbsent("${prefix}hepxE:$pdet",hmom).fill(epx.e())
                 hists.computeIfAbsent("${prefix}hepxT:$pdet",hmom).fill(Math.sqrt(epx.px()*epx.px() + epx.py()*epx.py()))
                 hists.computeIfAbsent("${prefix}hepxZ:$pdet",hmom).fill(epx.pz())
                 hists.computeIfAbsent("${prefix}hgg",hgg).fill(gg.mass())
                 //hists.computeIfAbsent("${prefix}hgg:$ihel",hgg).fill(gg.mass())
                 hists.computeIfAbsent("${prefix}hggpi0mom",hggpi0mom).fill(gg.p(), gg.mass())
              }
              return ispi0
            }
          }
          return false
        }

        [['',true],['gg/',pi0s.any()]].findAll{it[1]}.each{prefix,test->
          hists.computeIfAbsent("${prefix}hw:$psec:$pdet",hw).fill(wvec.mass())
          hists.computeIfAbsent("${prefix}hq2:$psec:$pdet",hq2).fill(-qvec.mass2())
          hists.computeIfAbsent("${prefix}hepx:$psec:$pdet",hepx0).fill(epx.mass2())
        }

        return isep0
      }

      // def goodgammas = ele.vect().theta(g1.vect())>8 && ele.vect().theta(g2.vect())>8
      // def prelims = goodgammas && epx.mass2()<1 && g1stat>=2000 && g2stat>=2000 && fgg>0.05 && fgg<0.3 && epggx.e()<1 && ftheta<16
      // def finals = prelims && ftheta<2 && fmisse<0.9 && epx.mass2().abs()<0.7
    }
  }
}
