#!/usr/bin/groovy

import java.io.*
import java.util.*
import org.jlab.groot.data.TDirectory
import org.jlab.groot.data.GraphErrors
import org.jlab.groot.group.DataGroup
import org.jlab.groot.data.H1F
import org.jlab.groot.data.H2F
import org.jlab.groot.math.F1D
import org.jlab.groot.fitter.DataFitter
import org.jlab.io.base.DataBank
import org.jlab.io.base.DataEvent
import org.jlab.io.hipo.HipoDataSource
import org.jlab.io.hipo.HipoDataSync
import org.jlab.detector.base.DetectorType
import org.jlab.clas.physics.Vector3
import org.jlab.clas.physics.LorentzVector
import org.jlab.groot.base.GStyle
import org.jlab.groot.graphics.EmbeddedCanvas

float EB = 10.6f
int run = args[0].split("/")[-1].split('\\.')[0][-4..-1].toInteger()
if(run>6607) EB=10.2f

def Hist_beta_p 	= [:].withDefault{new H2F("Hist_beta_p${it}"		, "Beta vs. Momentum ${it}"		          ,800,0,EB,300,  -0.1   ,1.2)}
def Hist_deltaB_p = [:].withDefault{new H2F("Hist_deltaB_p${it}"	, "Delta B vs. Momentum ${it}"          ,800,0,EB,1600,  -1     ,1  )}
def Hist_beta_p2 	= [:].withDefault{new H2F("Hist_beta_p2${it}"	  , "Beta (path/time) vs. Momentum ${it}"	,800,0,EB,300,  -0.1   ,1.2)}

for(fname in args) {
  def reader = new HipoDataSource()
  reader.open(fname)
  while(reader.hasEvent()) {
  //for(int ii=0;ii<=6;ii++){
  //println("On event number $ii")
    def event = reader.getNextEvent()
    if(!event.hasBank("REC::Particle")){
//	println("event bank empty, skipping")
	 continue
	}

    def recon_Particles = event.getBank("REC::Particle")
    def recon_Cal = event.getBank("REC::Calorimeter")
    def recon_Scint = event.getBank("REC::Scintillator")
    def momenta = ['x','y','z'].collect{recon_Particles.getFloat('p'+it)}.transpose().collect{Math.sqrt(it.collect{x->x*x}.sum())}
    def event_start_time = event.getBank("REC::Event").getFloat("startTime")[0]
    if(event_start_time<0){
      //printx("Event time was $event_start_time, skipping")
	 continue
	}
    def stati = recon_Particles.getInt('status')
    def pind_sarray = recon_Scint.getShort('pindex')*.toInteger()
    def pind_carray = recon_Cal.getShort('pindex')*.toInteger()
  //  println("scintillator interaction index array is $pind_sarray")
   // println("calorimeter interaction index array is $pind_carray")
 //   println("particle status is $stati")



	def scint_times = [recon_Scint.getShort('pindex')*.toInteger(), recon_Scint.getFloat('time')].transpose().collectEntries()
	def scint_paths = [recon_Scint.getShort('pindex')*.toInteger(), recon_Scint.getFloat('path')].transpose().collectEntries()
	def scint_layers = [recon_Scint.getShort('pindex')*.toInteger(), recon_Scint.getInt('layer')].transpose().collectEntries()
//	println("layer in scints are: $scint_layers")
//

	def scint_sectors = [recon_Scint.getShort('pindex')*.toInteger(), recon_Scint.getInt('sector')].transpose().collectEntries()
//	println("sectors in scints are: $scint_sectors")

	def secs = [recon_Scint.getShort('pindex')*.toInteger(), recon_Scint.getInt('detector')].transpose().collectEntries()
     //   println("secs is: $secs")

	def cecs = [recon_Cal.getShort('pindex')*.toInteger(), recon_Cal.getInt('detector')].transpose().collectEntries()
       // println("cecs is: $cecs")


	for(int p_ind=0;p_ind<event.getBank("REC::Particle").rows();p_ind++){ //Loop over all particles in the event
      if(!(recon_Particles.getInt("charge",p_ind)>0)){ continue  }
  		//if(!recon_Particles.getInt("pid",p)==2212) continue;

      		float p_momentum = momenta[p_ind]
  		float beta_recon = recon_Particles.getFloat("beta",p_ind)
  		float p_mass = 0.938 //Proton mass in GeV
  		float beta_calc = Math.sqrt(p_momentum*p_momentum/(p_momentum*p_momentum+p_mass*p_mass))

		//def secs = [recon_Scint.getShort('pindex')*.toInteger(), recon_Scint.getInt('detector')].transpose().collectEntries()
    		//println(secs)
	//	println("particle number index is $p_ind")
//		println("scint response for $p_ind is: "+secs[p_ind]+" layer: "+scint_layers[p_ind]+" sector: "+scint_sectors[p_ind])
	//	println("cal response is: "+cecs[p_ind])
	//	println("status of particle is: "+stati[p_ind])
	//	println("XXXXXX end of particle info XXXXXX")
		//println("particle index is: $p_ind")
		//println("total recon is: "+ recon_Scint.getInt("detector"))
		//println("recon selected scint is: "+ recon_Scint.getInt("detector",p_ind))



	//	if(secs[p_ind]==12){
	//		println("using scint data")
	//		p_layer = recon_Scint.getInt("layer",p_ind)
        //               p_sect = recon_Scint.getInt("sector",p_ind)
	//		println(p_layer + "  is layer, sector is: "+p_sect)
	//	}
	//	if(cecs[p_ind]==7){
	//		println("using calorimeter data")
	//		p_layer = recon_Cal.getInt("layer",p_ind)
          //              p_sect = recon_Cal.getInt("sector",p_ind)
	//		println(p_layer + "  is layer, sector is: "+p_sect)
	//	}


  		//if(recon_Scint.getInt("detector",p_ind)==12){

		if(secs[p_ind]==12){
		//if(recon_Particles.getInt('status')[p_ind]>1999){
	  	//	if(recon_Particles.getInt('status')[p_ind]<3999){
				p_layer = scint_layers[p_ind]
    				p_sect = scint_sectors[p_ind]
    				p_time = scint_times[p_ind]
    				p_path = scint_paths[p_ind]
  		  		p_comp =recon_Scint.getInt('component',p_ind)
      				//  println("particle status is: "+recon_Particles.getInt('status')[p_ind])


  		if ([1, 2, 3, 4, 5, 6].contains(p_sect) && [1, 2, 3].contains(p_layer)){
  		    title = "sec${p_sect}_layer${p_layer}"
  				Hist_beta_p[title].fill(p_momentum,beta_recon)
  				Hist_deltaB_p[title].fill(p_momentum,beta_recon-beta_calc)
  				Hist_beta_p2[title].fill(p_momentum,p_path/(p_time-event_start_time)/29.98)//p_time,p_comp)
  			//println("value is: "+(beta_recon-p_path/(p_time-event_start_time)/29.98)
      }
		else{
			println("Value not contained!")
			println(p_sect)
			println(p_layer)
		}}
    }
  }
  reader.close()
}


TDirectory out = new TDirectory()
out.mkdir('/'+run)
out.cd('/'+run)

for(int isec=1;isec<=6;isec++){
 for(int ilay=1;ilay<=3;ilay++){
	 title = "sec${isec}_layer${ilay}"
	 out.addDataSet(Hist_beta_p[title])
	 out.addDataSet(Hist_deltaB_p[title])
	 out.addDataSet(Hist_beta_p2[title])
 }
}

out.writeFile('pID_new_protons_'+run+'.hipo')
