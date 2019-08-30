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

def Hist_beta_p 				= [:].withDefault{new H2F("Hist_beta_p${it}"		, "Beta vs. Momentum ${it}"		 ,800,0,EB,100,0,1)}
def Hist_deltaB_p 			= [:].withDefault{new H2F("Hist_deltaB_p${it}"	, "Delta B vs. Momentum ${it}" ,800,0,EB,100,-1,1)}

for(fname in args) {
  def reader = new HipoDataSource()
  reader.open(fname)
  while(reader.hasEvent()) {
  //for(int ii=1;ii<=100;ii++){
  //println("On event number $ii")
  def event = reader.getNextEvent()
    if(!event.hasBank("REC::Particle")) continue


  	//float startTime = event.getBank("REC::Event").getFloat("startTime",0);
  	DataBank reconstructedParticle = event.getBank("REC::Particle")

  	for(int p_ind=0;p_ind<event.getBank("REC::Particle").rows();p_ind++){ //Loop over all particles in the event
  		//println("Particle number is $p_ind")
  		if(!(reconstructedParticle.getInt("charge",p_ind)>0)){
		//println("charge of " + reconstructedParticle.getInt("charge",p_ind)+" trying to break")
		continue
		}
  		//println("positive charge of " + reconstructedParticle.getInt("charge",p_ind)+" continuing")
  		//if(!reconstructedParticle.getInt("pid",p)==2212) return false;
  		float px = reconstructedParticle.getFloat("px",p_ind)
  		float py = reconstructedParticle.getFloat("py",p_ind)
  		float pz = reconstructedParticle.getFloat("pz",p_ind)
  		float beta_recon = reconstructedParticle.getFloat("beta",p_ind)
  		float p_momentum = (float)Math.sqrt(px*px+py*py+pz*pz)
  		float p_vz = reconstructedParticle.getFloat("vz",p_ind)
  		float p_vx = reconstructedParticle.getFloat("vx",p_ind)
  		float p_vy = reconstructedParticle.getFloat("vy",p_ind)
  		Ve = new LorentzVector(px,py,pz,p_momentum)
  		float p_phi = (float) Math.toDegrees(Ve.phi())
  		float p_theta = (float) Math.toDegrees(Ve.theta())
  		float p_mass = 0.938 //Proton mass in GeV
  		float scale_factor = 0.30
  		float beta_calc = (float)Math.sqrt(p_momentum*p_momentum/(p_momentum*p_momentum+p_mass*p_mass))
  		float p_mom_up = p_momentum*(1+scale_factor)
  		float p_mom_low = p_momentum*(1-scale_factor)
  		float beta_upper = (float)Math.sqrt(p_mom_up*p_mom_up/(p_mom_up*p_mom_up+p_mass*p_mass))
  		float beta_lower = (float)Math.sqrt(p_mom_low*p_mom_low/(p_mom_low*p_mom_low+p_mass*p_mass))


                if(!(beta_recon>0)){
                //println("charge of " + reconstructedParticle.getInt("charge",p_ind)+" trying to break")
                continue
                }

		//println("beta recon is"+beta_recon)
		//println("betacalc is"+beta_calc)

  		//if(beta_recon<beta_upper || beta_recon>beta_lower) return

  		DataBank recon_Scint = event.getBank("REC::Scintillator")
  		if(recon_Scint.getInt("detector",p_ind)==12){
  		
		p_layer = recon_Scint.getInt("layer",p_ind)
  		p_sect = recon_Scint.getInt("sector",p_ind)
  		p_time = recon_Scint.getFloat("time",p_ind)
  		p_path = recon_Scint.getFloat("path",p_ind)

		//println("beta recon is"+beta_recon)
                //println("betacalc is"+beta_calc)
		//println("layer is"+p_layer)
                //println("sector is"+p_sect)



		
  		if ([1, 2, 3, 4, 5, 6].contains(p_sect) && [1, 2, 3].contains(p_layer)){
  		    title = "sec${p_sect}_layer${p_layer}"
  				Hist_beta_p[title].fill(p_momentum,beta_recon)
  				Hist_deltaB_p[title].fill(p_momentum,beta_recon-beta_calc)
  			}
  	}}
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
 }
}

out.writeFile('proton_pID_new'+run+'.hipo')
