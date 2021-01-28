{
    Double_t Mass_p = 0.938;
    
    TLorentzVector p4_beam;
    TLorentzVector p4_pi0;

    TLorentzVector p4_target;
    TLorentzVector p4_proton;
    TLorentzVector p4_electron;
    TLorentzVector p4_gamma1;
    TLorentzVector p4_gamma2;
    TLorentzVector vgs;
    TLorentzVector vmG;
    TLorentzVector vmP;
    TLorentzVector vM;
    
    TVector3 v1;
    TVector3 v2;
    TVector3 v3l;
    TVector3 v3h;
    TVector3 vtemp1;
    TVector3 vtemp2;
    
    
    p4_beam.SetXYZM(0,0,10.604,0);
    p4_target.SetXYZM(0,0,0,Mass_p);
    
 // List of original variables 
	 Int_t old_nmb;
	 Float_t old_Pp[1000];
	 Float_t old_Ppx[1000];
	 Float_t old_Ppy[1000];
	 Float_t old_Ppz[1000];
	 Float_t old_Ptheta[1000];
	 Float_t old_Pphi[1000];
	 Float_t old_Pvx[1000];
	 Float_t old_Pvy[1000];
	 Float_t old_Pvz[1000];
	 Float_t old_Pvt[1000];
	 Float_t old_Pbeta[1000];
	 Int_t old_Pstat[1000];
	 Int_t old_PorigIndx[1000];
	 Int_t old_nml;
	 Float_t old_Ep[1000];
	 Float_t old_Epx[1000];
	 Float_t old_Epy[1000];
	 Float_t old_Epz[1000];
	 Float_t old_Etheta[1000];
	 Float_t old_Ephi[1000];
	 Float_t old_Evx[1000];
	 Float_t old_Evy[1000];
	 Float_t old_Evz[1000];
	 Float_t old_Evt[1000];
	 Float_t old_Ebeta[1000];
	 Int_t old_Estat[1000];
	 Int_t old_EorigIndx[1000];
	 Int_t old_nmg;
	 Float_t old_Gp[1000];
	 Float_t old_Gpx[1000];
	 Float_t old_Gpy[1000];
	 Float_t old_Gpz[1000];
	 Float_t old_Gtheta[1000];
	 Float_t old_Gphi[1000];
	 Float_t old_Gvx[1000];
	 Float_t old_Gvy[1000];
	 Float_t old_Gvz[1000];
	 Float_t old_Gvt[1000];
	 Float_t old_Gbeta[1000];
	 Int_t   old_Gstat[1000];
	 Int_t   old_GorigIndx[1000];
	 Float_t old_beamQ;
	 Float_t old_liveTime;
	 Float_t old_startTime;
	 Float_t old_RFTime;
	 Int_t old_helicity;
	 Int_t old_helicityRaw;
	 Long_t old_EventNum;
	 Long_t old_RunNum;
	 Float_t old_Q2[1000];
	 Float_t old_Nu[1000];
	 Float_t old_q[1000];
	 Float_t old_qx[1000];
	 Float_t old_qy[1000];
	 Float_t old_qz[1000];
	 Float_t old_W2[1000];
	 Float_t old_xB[1000];
	 Float_t old_t[1000];

    
    

 // List of branches in the original tree

    TChain *T=new TChain("T");
    T->Add("ntuple.root");
	 T->SetBranchAddress("nmb",&old_nmb);
	 T->SetBranchAddress("Pp",&old_Pp);
	 T->SetBranchAddress("Ppx",&old_Ppx);
	 T->SetBranchAddress("Ppy",&old_Ppy);
	 T->SetBranchAddress("Ppz",&old_Ppz);
	 T->SetBranchAddress("Ptheta",&old_Ptheta);
	 T->SetBranchAddress("Pphi",&old_Pphi);
	 T->SetBranchAddress("Pvx",&old_Pvx);
	 T->SetBranchAddress("Pvy",&old_Pvy);
	 T->SetBranchAddress("Pvz",&old_Pvz);
	 T->SetBranchAddress("Pvt",&old_Pvt);
	 T->SetBranchAddress("Pbeta",&old_Pbeta);
	 T->SetBranchAddress("Pstat",&old_Pstat);
	 T->SetBranchAddress("PorigIndx",&old_PorigIndx);
	 T->SetBranchAddress("nml",&old_nml);
	 T->SetBranchAddress("Ep",&old_Ep);
	 T->SetBranchAddress("Epx",&old_Epx);
	 T->SetBranchAddress("Epy",&old_Epy);
	 T->SetBranchAddress("Epz",&old_Epz);
	 T->SetBranchAddress("Etheta",&old_Etheta);
	 T->SetBranchAddress("Ephi",&old_Ephi);
	 T->SetBranchAddress("Evx",&old_Evx);
	 T->SetBranchAddress("Evy",&old_Evy);
	 T->SetBranchAddress("Evz",&old_Evz);
	 T->SetBranchAddress("Evt",&old_Evt);
	 T->SetBranchAddress("Ebeta",&old_Ebeta);
	 T->SetBranchAddress("Estat",&old_Estat);
	 T->SetBranchAddress("EorigIndx",&old_EorigIndx);
	 T->SetBranchAddress("nmg",&old_nmg);
	 T->SetBranchAddress("Gp",&old_Gp);
	 T->SetBranchAddress("Gpx",&old_Gpx);
	 T->SetBranchAddress("Gpy",&old_Gpy);
	 T->SetBranchAddress("Gpz",&old_Gpz);
	 T->SetBranchAddress("Gtheta",&old_Gtheta);
	 T->SetBranchAddress("Gphi",&old_Gphi);
	 T->SetBranchAddress("Gvx",&old_Gvx);
	 T->SetBranchAddress("Gvy",&old_Gvy);
	 T->SetBranchAddress("Gvz",&old_Gvz);
	 T->SetBranchAddress("Gvt",&old_Gvt);
	 T->SetBranchAddress("Gbeta",&old_Gbeta);
	 T->SetBranchAddress("Gstat",&old_Gstat);
	 T->SetBranchAddress("GorigIndx",&old_GorigIndx);
	 T->SetBranchAddress("beamQ",&old_beamQ);
	 T->SetBranchAddress("liveTime",&old_liveTime);
	 T->SetBranchAddress("startTime",&old_startTime);
	 T->SetBranchAddress("RFTime",&old_RFTime);
	 T->SetBranchAddress("helicity",&old_helicity);
	 T->SetBranchAddress("helicityRaw",&old_helicityRaw);
	 T->SetBranchAddress("EventNum",&old_EventNum);
	 T->SetBranchAddress("RunNum",&old_RunNum);
	 T->SetBranchAddress("Q2",&old_Q2);
	 T->SetBranchAddress("Nu",&old_Nu);
	 T->SetBranchAddress("q",&old_q);
	 T->SetBranchAddress("qx",&old_qx);
	 T->SetBranchAddress("qy",&old_qy);
	 T->SetBranchAddress("qz",&old_qz);
	 T->SetBranchAddress("W2",&old_W2);
	 T->SetBranchAddress("xB",&old_xB);
	 T->SetBranchAddress("t",&old_t);



 // New tree

	 TFile *f=new TFile("Updated_pi0.root","recreate");
	 TTree *T1 = new TTree("T","");
 // List of NEW variables 
	 Int_t nmb;
	 Float_t Pp[100];
	 Float_t Ppx[100];
	 Float_t Ppy[100];
	 Float_t Ppz[100];
	 Float_t Ptheta[100];
	 Float_t Pphi[100];
	 Float_t Pvx[100];
	 Float_t Pvy[100];
	 Float_t Pvz[100];
	 Float_t Pvt[100];
	 Float_t Pbeta[100];
	 Int_t Pstat[100];
	 Int_t PorigIndx[100];
    Int_t PSector[100];
	 Int_t nml;
	 Float_t Ep;
	 Float_t Epx;
	 Float_t Epy;
	 Float_t Epz;
	 Float_t Etheta;
	 Float_t Ephi;
	 Float_t Evx;
	 Float_t Evy;
	 Float_t Evz;
	 Float_t Evt;
	 Float_t Ebeta;
	 Int_t Estat;
    Int_t ESector;

	 Int_t nmg;
	 Float_t Gp[100];
	 Float_t Gpx[100];
	 Float_t Gpy[100];
	 Float_t Gpz[100];
	 Float_t Gtheta[100];
	 Float_t Gphi[100];
	 Float_t Gvx[100];
	 Float_t Gvy[100];
	 Float_t Gvz[100];
	 Float_t Gvt[100];
	 Float_t Gbeta[100];
	 Int_t Gstat[100];
    Int_t GSector[100];
	 Float_t beamQ;
	 Float_t liveTime;
	 Float_t startTime;
	 Float_t RFTime;
	 Int_t helicity;
	 Int_t helicityRaw;
	 Long_t EventNum;
	 Long_t RunNum;
	 Float_t Q2;
	 Float_t Nu;
	 Float_t q;
	 Float_t qx;
	 Float_t qy;
	 Float_t qz;
	 Float_t W2;
	 Float_t xB;
	 Float_t t[100];
	 Int_t combint;
    
    
    Float_t Pi0p[1000];
    Float_t Pi0px[1000];
    Float_t Pi0py[1000];
    Float_t Pi0pz[1000];
    Float_t Pi0theta[1000];
    Float_t Pi0phi[1000];
    Float_t Pi0M[1000];
    Int_t Pi0Sector[1000];



	 Float_t tE[1000];

    Float_t mPpx[1000];
    Float_t mPpy[1000];
    Float_t mPpz[1000];
    Float_t mPp[1000];
	 Float_t mmP[1000];
    Float_t meP[1000];

     Float_t Mpx[1000];
	 Float_t Mpy[1000];
	 Float_t Mpz[1000];
	 Float_t Mp[1000];
     Float_t mm[1000];
     Float_t me[1000];
    
    
     Float_t mGpx[1000];
	 Float_t mGpy[1000];
	 Float_t mGpz[1000];
	 Float_t mGp[1000];
     Float_t meG[1000];
     Float_t mmG[1000];
	 
     Int_t pIndex[1000];
	 Int_t gIndex1[1000];
     Int_t gIndex2[1000];
    
	 Float_t trento[1000];
	 Float_t trento2[1000];
    Float_t trento3[1000];

	 Float_t theta1[1000];
	 Float_t theta2[1000];


 // List of NEW branches in the NEW tree

	 T1->Branch("nmb",&nmb,"nmb/I");
	 T1->Branch("Pp",&Pp,"Pp[nmb]/F");
	 T1->Branch("Ppx",&Ppx,"Ppx[nmb]/F");
	 T1->Branch("Ppy",&Ppy,"Ppy[nmb]/F");
	 T1->Branch("Ppz",&Ppz,"Ppz[nmb]/F");
	 T1->Branch("Ptheta",&Ptheta,"Ptheta[nmb]/F");
	 T1->Branch("Pphi",&Pphi,"Pphi[nmb]/F");
	 T1->Branch("Pvx",&Pvx,"Pvx[nmb]/F");
	 T1->Branch("Pvy",&Pvy,"Pvy[nmb]/F");
	 T1->Branch("Pvz",&Pvz,"Pvz[nmb]/F");
	 T1->Branch("Pvt",&Pvt,"Pvt[nmb]/F");
    T1->Branch("PSector",&PSector,"PSector[nmb]/I");

	 T1->Branch("Pbeta",&Pbeta,"Pbeta[nmb]/F");
	 T1->Branch("Pstat",&Pstat,"Pstat[nmb]/I");
	 T1->Branch("nml",&nml,"nml/I");
	 T1->Branch("Ep",&Ep,"Ep/F");
	 T1->Branch("Epx",&Epx,"Epx/F");
	 T1->Branch("Epy",&Epy,"Epy/F");
	 T1->Branch("Epz",&Epz,"Epz/F");
	 T1->Branch("Etheta",&Etheta,"Etheta/F");
	 T1->Branch("Ephi",&Ephi,"Ephi/F");
	 T1->Branch("Evx",&Evx,"Evx/F");
	 T1->Branch("Evy",&Evy,"Evy/F");
	 T1->Branch("Evz",&Evz,"Evz/F");
	 T1->Branch("Evt",&Evt,"Evt/F");
	 T1->Branch("Ebeta",&Ebeta,"Ebeta/F");
	 T1->Branch("Estat",&Estat,"Estat/I");
    T1->Branch("ESector",&ESector,"ESector/I");

	 T1->Branch("nmg",&nmg,"nmg/I");
	 T1->Branch("Gp",&Gp,"Gp[nmg]/F");
	 T1->Branch("Gpx",&Gpx,"Gpx[nmg]/F");
	 T1->Branch("Gpy",&Gpy,"Gpy[nmg]/F");
	 T1->Branch("Gpz",&Gpz,"Gpz[nmg]/F");
	 T1->Branch("Gtheta",&Gtheta,"Gtheta[nmg]/F");
	 T1->Branch("Gphi",&Gphi,"Gphi[nmg]/F");
	 T1->Branch("Gvx",&Gvx,"Gvx[nmg]/F");
	 T1->Branch("Gvy",&Gvy,"Gvy[nmg]/F");
	 T1->Branch("Gvz",&Gvz,"Gvz[nmg]/F");
	 T1->Branch("Gvt",&Gvt,"Gvt[nmg]/F");
    T1->Branch("GSector",&GSector,"GSector[nmg]/I");

	 T1->Branch("Gbeta",&Gbeta,"Gbeta[nmg]/F");
	 T1->Branch("Gstat",&Gstat,"Gstat[nmg]/I");
	 T1->Branch("beamQ",&beamQ,"beamQ/F");
	 T1->Branch("liveTime",&liveTime,"liveTime/F");
	 T1->Branch("startTime",&startTime,"startTime/F");
	 T1->Branch("RFTime",&RFTime,"RFTime/F");
	 T1->Branch("helicity",&helicity,"helicity/I");
	 T1->Branch("helicityRaw",&helicityRaw,"helicityRaw/I");
	 T1->Branch("EventNum",&EventNum,"EventNum/L");
	 T1->Branch("RunNum",&RunNum,"RunNum/L");
	 T1->Branch("Q2",&Q2,"Q2/F");
	 T1->Branch("Nu",&Nu,"Nu/F");
	 T1->Branch("q",&q,"q/F");
	 T1->Branch("qx",&qx,"qx/F");
	 T1->Branch("qy",&qy,"qy/F");
	 T1->Branch("qz",&qz,"qz/F");
	 T1->Branch("W2",&W2,"W2/F");
	 T1->Branch("xB",&xB,"xB/F");
	 T1->Branch("t",&t,"t[nmb]/F");
	 T1->Branch("combint",&combint,"combint/I");
	 //T1->Branch("tE",&tE,"tE[combint]/F");
    
    
    T1->Branch("mPpx",&mPpx,"mPpx[combint]/F");
    T1->Branch("mPpy",&mPpy,"mPpy[combint]/F");
    T1->Branch("mPpz",&mPpz,"mPpz[combint]/F");
    T1->Branch("mPp",&mPp,"mPp[combint]/F");
     T1->Branch("mmP",&mmP,"mmP[combint]/F");
     T1->Branch("meP",&meP,"meP[combint]/F");
    
     T1->Branch("Mpx",&Mpx,"Mpx[combint]/F");
	 T1->Branch("Mpy",&Mpy,"Mpy[combint]/F");
	 T1->Branch("Mpz",&Mpz,"Mpz[combint]/F");
	 T1->Branch("Mp",&Mp,"Mp[combint]/F");
     T1->Branch("mm",&mm,"mm[combint]/F");
     T1->Branch("me",&me,"me[combint]/F");

    
	 T1->Branch("mGpx",&mGpx,"mGpx[combint]/F");
	 T1->Branch("mGpy",&mGpy,"mGpy[combint]/F");
	 T1->Branch("mGpz",&mGpz,"mGpz[combint]/F");
	 T1->Branch("mGp",&mGp,"mGp[combint]/F");
     T1->Branch("mmG",&mmG,"mmG[combint]/F");
     T1->Branch("meG",&meG,"meG[combint]/F");

    T1->Branch("Pi0p",&Pi0p,"Pi0p[combint]/F");
    T1->Branch("Pi0px",&Pi0px,"Pi0px[combint]/F");
    T1->Branch("Pi0py",&Pi0py,"Pi0py[combint]/F");
    T1->Branch("Pi0pz",&Pi0pz,"Pi0pz[combint]/F");
    T1->Branch("Pi0theta",&Pi0theta,"Pi0theta[combint]/F");
    T1->Branch("Pi0phi",&Pi0phi,"Piphi[combint]/F");
    T1->Branch("Pi0M",&Pi0M,"Pi0M[combint]/F");
    T1->Branch("Pi0Sector",&Pi0Sector,"Pi0Sector[combint]/I");

    
     T1->Branch("pIndex",&pIndex,"pIndex[combint]/I");
	 T1->Branch("gIndex1",&gIndex1,"gIndex1[combint]/I");
     T1->Branch("gIndex2",&gIndex2,"gIndex2[combint]/I");
	 T1->Branch("trento",&trento,"trento[combint]/F");
	 T1->Branch("trento2",&trento2,"trento2[combint]/F");
    T1->Branch("trento3",&trento3,"trento3[combint]/F");
/*	 T1->Branch("theta1",&theta1,"theta1[combint]/F");
	 T1->Branch("theta2",&theta2,"theta2[combint]/F");
*/

	 Long_t nEvents = T->GetEntries();
    
    Int_t nGind=0;
    Int_t nPind=0;
    Int_t counter;
    Int_t nComb;
    
    Int_t goodPind[1000];
    Int_t goodGind[1000];
    Int_t goodComb[1000];
    Int_t GnewIndex[1000];
    Int_t PnewIndex[1000];
    Int_t tempP[1000];
    Int_t tempG[1000];
    

    cout<<" total number of events = "<< nEvents<<endl;
	 for(Long_t ev =0; ev<nEvents; ev++){
         
         if(ev%100000 == 0) cout<<" analyzed = "<<ev<<endl;
		 T->GetEvent(ev);
         if(old_nml==1 && old_Q2[0]>1 && old_xB[0]<1 && TMath::Sqrt(old_W2[0])>2 && old_Ep[0]>2.1 )
          {// continue;

          //   if(old_Q2[0]<=1) continue;
            // if(old_xB[0]>=1) continue;
             //if(TMath::Sqrt(old_W2[0])<=2 ) continue;
             //if(old_Ep[0]<=2.1) continue;


     // Fill the electron branches in the new tree

             nml = 1;

             Ep = old_Ep[0];
             Epx = old_Epx[0];
             Epy = old_Epy[0];
             Epz = old_Epz[0];
             Etheta = old_Etheta[0];
             Ephi = old_Ephi[0];
             Evx = old_Evx[0];
             Evy = old_Evy[0];
             Evz = old_Evz[0];
             Evt = old_Evt[0];
             Ebeta = old_Ebeta[0];
             Estat = old_Estat[0];
              
              Ephi =  Ephi*180./TMath::Pi();
              if(Ephi < -30.){
                  Ephi +=360.0;
              }
              else if(Ephi > 330.0){
                  Ephi -=360.0;
              }
      
              ESector = int((Ephi + 90.)/60.0) - 1.;
      
              
             Q2 = old_Q2[0];
             Nu = old_Nu[0];
             q = old_q[0];
             qx = old_qx[0];
             qy = old_qy[0];
             qz = old_qz[0];
             W2 = old_W2[0];
             xB = old_xB[0];
             beamQ = old_beamQ;
             liveTime = old_liveTime;
             startTime = old_startTime;
             RFTime = old_RFTime;
             helicity = old_helicity;
             helicityRaw = old_helicityRaw;
             EventNum = old_EventNum;
             RunNum = old_RunNum;
             
             
             
              nGind=0;
             for(Int_t kk=0;kk<old_nmg; kk++){
                         Gp[nGind] = old_Gp[kk];
                         Gpx[nGind] = old_Gpx[kk];
                         Gpy[nGind] = old_Gpy[kk];
                         Gpz[nGind] = old_Gpz[kk];
                         Gtheta[nGind] = old_Gtheta[kk];
                         Gphi[nGind] = old_Gphi[kk];
                         Gvx[nGind] = old_Gvx[kk];
                         Gvy[nGind] = old_Gvy[kk];
                         Gvz[nGind] = old_Gvz[kk];
                         Gvt[nGind] = old_Gvt[kk];
                         Gbeta[nGind] = old_Gbeta[kk];
                         Gstat[nGind] = old_Gstat[kk];
         
                 Gphi[nGind] =  Gphi[nGind]*180./TMath::Pi();
                 if(Gphi[nGind] < -30.){
                     Gphi[nGind] +=360.0;
                 }
                 else if(Gphi[nGind] > 330.0){
                     Gphi[nGind] -=360.0;
                 }
         
                 GSector[nGind] = int((Gphi[nGind] + 90.)/60.0) - 1.;
         
                 
                 
                         nGind++;

             }
             nmg = nGind;
             
             
             nPind =0;
             for(Int_t kk=0;kk<old_nmb; kk++){
                         Pp[nPind] = old_Pp[kk];
                         Ppx[nPind] = old_Ppx[kk];
                         Ppy[nPind] = old_Ppy[kk];
                         Ppz[nPind] = old_Ppz[kk];
                         Ptheta[nPind] = old_Ptheta[kk];
                         Pphi[nPind] = old_Pphi[kk];
                         Pvx[nPind] = old_Pvx[kk];
                         Pvy[nPind] = old_Pvy[kk];
                         Pvz[nPind] = old_Pvz[kk];
                         Pvt[nPind] = old_Pvt[kk];
                         Pbeta[nPind] = old_Pbeta[kk];
                         Pstat[nPind] = old_Pstat[kk];
                         t[nPind] = old_t[kk];
                 
                         Pphi[nPind] =  Pphi[nPind]*180./TMath::Pi();
                         if(Pphi[nPind] < -30.){
                             Pphi[nPind] +=360.0;
                         }
                         else if(Pphi[nPind] > 330.0){
                             Pphi[nPind] -=360.0;
                         }
                 
                         PSector[nPind] = int((Pphi[nPind] + 90.)/60.0) - 1.;
                 
                         nPind++;
             }
             nmb = nPind;
             
             
             /// run over all possible combinations of epgg =============
             
             p4_electron.SetXYZM(Epx,Epy,Epz,0.000511);
             
             vgs = p4_beam - p4_electron;               // virtual photon
             
             combint = 0;
        //     cout<<" event line ===================================================================  "<<ev <<"   nmb = "<<old_nmb<<" pInd = "<<nPind<<"    nmg = "<< old_nmg<<"  gInd = "<<nGind<<endl;
             for(int np = 0;np<nPind;np++){
                 p4_proton.SetXYZM(Ppx[np],Ppy[np],Ppz[np],Mass_p);
                 
                 vmG = p4_beam + p4_target - p4_electron - p4_proton; // missing 4 vector for ep -> e'p
                 for(int ng1 = 0;ng1<nGind-1;ng1++){
                 //    cout<<" ng 1 = "<<ng1<<endl;
                     p4_gamma1.SetXYZM(Gpx[ng1],Gpy[ng1],Gpz[ng1],0); // first gamma
                     for(int ng2=ng1+1; ng2<nGind; ng2++){
                      //   cout<<" ng2 = "<<ng2<<" combint = "<<combint<<endl;
//                         cout<<" ng1 = "<<ng1<<" ng2 = "<<ng2<<" np = "<<np<<"  combin = "<<combint<<endl;
                         p4_gamma2.SetXYZM(Gpx[ng2],Gpy[ng2],Gpz[ng2],0); // second gamma
                         
                         vmP = p4_beam + p4_target - p4_electron - p4_gamma1 - p4_gamma2; // missing 4 vector for ep->egg reaction
                         vM  = p4_beam + p4_target - p4_electron - p4_proton - p4_gamma1 - p4_gamma2; // missing 4 vector for ep->epgg recation
                         
                         p4_pi0 = p4_gamma1 + p4_gamma2;
                         

                         mPpx[combint] = vmP.Px();
                         mPpy[combint] = vmP.Py();
                         mPpz[combint] = vmP.Pz();
                         mPp[combint]  = vmP.P();
                         mmP[combint] = vmP.M2();
                         meP[combint] = vmP.E();
                         
                         mGpx[combint] = vmG.Px();
                         mGpy[combint] = vmG.Py();
                         mGpz[combint] = vmG.Pz();
                         mGp[combint]  = vmG.P();
                         mmG[combint] = vmG.M2();
                         meG[combint] = vmG.E();

                         Mpx[combint] = vM.Px();
                         Mpy[combint] = vM.Py();
                         Mpz[combint] = vM.Pz();
                         Mp[combint]  = vM.P();
                         mm[combint]  = vM.M2();
                         me[combint]  = vM.E();
                         
                         pIndex[combint] = np;
                         gIndex1[combint] = ng1;
                         gIndex2[combint] = ng2;
                         
                         Pi0p[combint] = p4_pi0.P();
                         Pi0px[combint] = p4_pi0.Px();
                         Pi0py[combint] = p4_pi0.Py();
                         Pi0pz[combint] = p4_pi0.Pz();
                         Pi0theta[combint] = p4_pi0.Theta();
                         Pi0phi[combint]   = p4_pi0.Phi();
                         
                         Pi0phi[combint] =  Pi0phi[combint]*180./TMath::Pi();
                         if(Pi0phi[combint] < -30.){
                             Pi0phi[combint] +=360.0;
                         }
                         else if(Pi0phi[combint] > 330.0){
                             Pi0phi[combint] -=360.0;
                         }
                 
                         Pi0Sector[combint] = int((Pi0phi[combint] + 90.)/60.0) - 1.;
                         Pi0M[combint] = p4_pi0.M();
                   
                         
                         //===========
                         v1.SetXYZ(p4_beam.Px(),p4_beam.Py(),p4_beam.Pz());
                         v2.SetXYZ(p4_electron.Px(),p4_electron.Py(),p4_electron.Pz());
                         v3l = v1.Cross(v2);
                         
                         v1.SetXYZ(p4_proton.Px(),p4_proton.Py(),p4_proton.Pz());
                         v2.SetXYZ(vgs.Px(),vgs.Py(),vgs.Pz());
                         v3h = v1.Cross(v2);
                         
                         if( v1.Mag() * v2.Mag() !=0 && v1.Dot(v2)<v1.Mag()*v2.Mag() ) {
                             trento[combint] = TMath::ACos(v3l.Dot(v3h)/(v3l.Mag()*v3h.Mag()) ) * 180./TMath::Pi();
                         }
                         else trento[combint] =0;

                         if(v3l.Dot(v1) >0 ) trento[combint] = 360 - trento[combint];
                         //=================
                         // trento 2
                                 v2.SetXYZ(p4_electron.Px(),p4_electron.Py(),p4_electron.Pz());
                                 v1.SetXYZ(vgs.Px(),vgs.Py(),vgs.Pz());

                                 vtemp1 = v1.Cross(v2);
                                 v2.SetXYZ(p4_gamma1.Px(),p4_gamma1.Py(),p4_gamma1.Pz());

                                 vtemp2 = v1.Cross(v2);

                                 if( vtemp1.Mag() * vtemp2.Mag() !=0 && vtemp1.Dot(vtemp2)<vtemp1.Mag()*vtemp2.Mag() ) {
                                     trento2[combint] = TMath::ACos(vtemp1.Dot(vtemp2)/(vtemp1.Mag()*vtemp2.Mag()) ) * 180./TMath::Pi();
                                 }
                                 else trento2[combint] =0;

                                 if (v1.Dot(vtemp1.Cross(vtemp2))<0) trento2[combint] = 360 - trento2[combint];

                         //=================
                         // trento 3
                                 v2.SetXYZ(p4_electron.Px(),p4_electron.Py(),p4_electron.Pz());
                                 v1.SetXYZ(vgs.Px(),vgs.Py(),vgs.Pz());

                                 vtemp1 = v1.Cross(v2);
                                 v2.SetXYZ(p4_gamma2.Px(),p4_gamma2.Py(),p4_gamma2.Pz());

                                 vtemp2 = v1.Cross(v2);

                                 if( vtemp1.Mag() * vtemp2.Mag() !=0 && vtemp1.Dot(vtemp2)<vtemp1.Mag()*vtemp2.Mag() ) {
                                     trento3[combint] = TMath::ACos(vtemp1.Dot(vtemp2)/(vtemp1.Mag()*vtemp2.Mag()) ) * 180./TMath::Pi();
                                 }
                                 else trento3[combint] =0;

                                 if (v1.Dot(vtemp1.Cross(vtemp2))<0) trento3[combint] = 360 - trento3[combint];



                         
                         combint++;
                         
                     } // second loop over photons
                 } // first loop over photons
             } // loop over protons
             

             if(nmb>0 && nmg>0 && combint>0)
                 T1->Fill();
         }
	 } // end of events for loop
    
    f->Write();
    f->Close();
} // end of function 
