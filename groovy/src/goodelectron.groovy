import org.jlab.io.hipo.HipoDataSource
import org.jlab.clas.physics.LorentzVector
import uconn.utils.pid.stefan.ElectronCandidate
import uconn.utils.pid.stefan.ElectronCandidate.Cut
import uconn.utils.pid.stefan.ProtonCandidate
import uconn.utils.pid.stefan.ProtonCandidate.Cut
 

def banknames = ['RUN::config', 'REC::Particle','REC::Calorimeter','REC::Cherenkov','REC::Traj']
 
args.each{fname->
    def reader = new HipoDataSource()
    reader.open(fname)



    //FOR PROTONS
    for (int jjj=0; jjj < 100; jjj++){

        def pro_inds = []

        def event = reader.getNextEvent()
        if(banknames.every{event.hasBank(it)}) {
            def (runb,partb,calb,ccb,trajb) = banknames.collect{event.getBank(it)}

            for (int ipart in 0..<partb.rows()){
                def candidate = ProtonCandidate.getProtonCandidate(ipart, partb, trajb)

                //test for all cuts specified in ElectronCandidate class
                if(candidate.isproton(Cut.values())) {
                    //println("good electron is found")
                    pro_inds.add(ipart)
                    //println("electron momentum = "+ele.p())
                }
            }


        }
        println(pro_inds)
        
    }



    //FOR ELECTRONS
    // for (int jjj=0; jjj < 10; jjj++){

    //     def ele_inds = []

    //     def event = reader.getNextEvent()
    //     if(banknames.every{event.hasBank(it)}) {
    //     def (runb,partb,calb,ccb,trajb) = banknames.collect{event.getBank(it)}

    //     def ipart = 0
    //     def candidate = ElectronCandidate.getElectronCandidate(ipart, partb, calb, ccb, trajb)

    //     //test for all cuts specified in ElectronCandidate class
    //     if(candidate.iselectron()) {
    //     //println("good electron is found")
    //     def ele = candidate.getLorentzVector()
    //     //println("electron momentum = "+ele.p())
    //     }

    //     //specify the list of cuts you want to apply
    //     if(candidate.iselectron(Cut.ELE_PID, Cut.CC_NPHE, Cut.DC_VERTEX)) {
    //     //println("good electron is found")

    //         def ele_ind = ipart

    //         //def ele = candidate.getLorentzVector()

    //         ele_inds.add(ele_ind)

    //     //println("electron momentum = "+ele.p())
    //         println(ele_inds)
    //     }

    //     }
        
    // }

    reader.close()
}
 