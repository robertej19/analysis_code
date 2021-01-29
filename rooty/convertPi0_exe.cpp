#include "TFile.h"
#include "TTree.h"
#include "TLorentzVector.h"
#include "TVector3.h"
#include <string>
#include <iostream>
#include <vector>
#include "HipoChain.h"
#include "clas12reader.h"
#include <fstream>

//#include "/u/group/clas12/packages/clas12root/1.6/Clas12Root/TRcdbVals.h"
//#include "rcdb_vals.h"

//#include "/u/group/clas12/packages/clas12root/1.6/Clas12Banks/clas12reader.h"
//#include "/u/group/clas12/packages/clas12root/1.6/Clas12Banks/rcdb_vals.h"
//#include "/u/group/clas12/packages/clas12root/1.6/Clas12Root/TRcdbVals.h"
//#include "HipoChain.h"


using namespace std;

int main(int argc, char **argv){
    clas12root::HipoChain chain;

	if(argc == 1){
		char File[200];
		system("ls -1 *.hipo > dataFiles.txt");
		ifstream in("dataFiles.txt", ios::in);
		if(!in){
			cerr<< "File Not Opened!" <<endl;
			exit(1);
		}
		while( in >> File){
			cout<<" File Name = "<<File<<endl;
			chain.Add(File);	
		}
	}

	
    //chain.Add("/work/clas12/igorko/HipoFiles/dvcs_RGA_IN_filtered_with_TOF.hipo");
    TLorentzVector p4_proton[100];
    TLorentzVector p4_electron[100];


    TLorentzVector p4_gamma[100];
    TLorentzVector p4_pi0;
    TLorentzVector p4_beam;
    TLorentzVector p4_target;
    
    p4_beam.SetXYZM(0,0,10.604,0);
    p4_target.SetXYZM(0,0,0,0.938);
    
    
    TFile *rFile = TFile::Open("ntuple.root","RECREATE");
    TTree *T=new TTree("T","epg");


    // Define variables

    Float_t mp  =0.938;
    // === beam ======
    // incoming Beam energy
    Float_t Q2[100];
    Float_t Nu[100];
    Float_t xB[100];
    Float_t t[100];
    Float_t q[100];
    Float_t qx[100];
    Float_t qy[100];
    Float_t qz[100];
    Float_t W2[100];
//   Event and run parameters

	Float_t beamQ;
	Float_t liveTime;
	Float_t startTime;
	Float_t RFTime;
	Int_t   helicity;
	Int_t   helicityRaw;
	Long_t  EventNum;
	Long_t  RunNum;
// Physics quantities
//
//
Int_t combint;
Float_t meG[1000] ;
Float_t meP[1000] ;
Float_t me[1000]  ;
Float_t tE[1000]  ;
Float_t mmG[1000] ;
Float_t mm[1000]  ;
Float_t mmP[1000] ;
Float_t Mpx[1000] ;
Float_t Mpy[1000] ;
Float_t Mpz[1000] ;
Float_t Mp[1000]  ;
Float_t mGpx[1000];
Float_t mGpy[1000];
Float_t mGpz[1000];
Float_t mGp[1000];
Int_t pIndex[1000];
Int_t gIndex[1000]; 
    Float_t theta1[1000];
    Float_t theta2[1000];
    Float_t trento[1000];
    Float_t trento2[1000];

   // =====  proton =====
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

    // ==== electron =====
    Int_t nml;
    Float_t Ep[100];
    Float_t Epx[100];
    Float_t Epy[100];
    Float_t Epz[100];
    Float_t Etheta[100];
    Float_t Ephi[100];
    Float_t Evx[100];
    Float_t Evy[100];
    Float_t Evz[100];
    Float_t Evt[100];
    Float_t Ebeta[100];
    Int_t Estat[100];
    Int_t EorigIndx[100];
    
    
    
    // ==== gammas =====
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
    Int_t GorigIndx[100];






vector<Int_t> vScInd;
vector<Int_t> vScPnd;
vector<Int_t> vScDet;
vector<Int_t> vScSet;
vector<Int_t> vScLay;
vector<Int_t> vScCom;
vector<Double_t> vScEne;
vector<Double_t> vScTim;
vector<Double_t> vScPat;
vector<Double_t> vScX;
vector<Double_t> vScY;
vector<Double_t> vScZ;
vector<Int_t> vScStat;

 
    //===========   additional missing masses ======
    


/*
   Float_t PdcX[100][3];
   Float_t PdcY[100][3];
   Float_t PdcZ[100][3];
*/
   Int_t Before[100];
    
    //=========== Tree branches ===============
    
//    T->Branch("mmG",&mmG,"mmG/F");
//    T->Branch("mmP",&mmP,"mmP/F");
//    T->Branch("mm",&mm,"mm/F");
    
    
   
 ///   protons ================================== 
    T->Branch("nmb",&nmb,"nmb/I");
    T->Branch("Pp",&Pp,"Pp[nmb]/F");
    T->Branch("Ppx",&Ppx,"Ppx[nmb]/F");
    T->Branch("Ppy",&Ppy,"Ppy[nmb]/F");
    T->Branch("Ppz",&Ppz,"Ppz[nmb]/F");
    T->Branch("Ptheta",&Ptheta,"Ptheta[nmb]/F");
    T->Branch("Pphi",&Pphi,"Pphi[nmb]/F");
//    T->Branch("Ptime",&Ptime,"Ptime[nmb]/F");
//   T->Branch("PdcX",&PdcX,"PdcX[nmb][3]/F");
//   T->Branch("PdcY",&PdcY,"PdcY[nmb][3]/F");
//   T->Branch("PdcZ",&PdcZ,"PdcZ[nmb][3]/F");    

    T->Branch("Pvx",&Pvx,"Pvx[nmb]/F");
    T->Branch("Pvy",&Pvy,"Pvy[nmb]/F");
    T->Branch("Pvz",&Pvz,"Pvz[nmb]/F");
    T->Branch("Pvt",&Pvy,"Pvt[nmb]/F");
    T->Branch("Pbeta",&Pbeta,"Pbeta[nmb]/F");
    T->Branch("Pstat",&Pstat,"Pstat[nmb]/I");
    T->Branch("PorigIndx",&PorigIndx,"PorigIndx[nmb]/I");


// ===============    Electrons ==============    
    T->Branch("nml",&nml,"nml/I");
    T->Branch("Ep",&Ep,"Ep[nml]/F");
    T->Branch("Epx",&Epx,"Epx[nml]/F");
    T->Branch("Epy",&Epy,"Epy[nml]/F");
    T->Branch("Epz",&Epz,"Epz[nml]/F");
    T->Branch("Etheta",&Etheta,"Etheta[nml]/F");
    T->Branch("Ephi",&Ephi,"Ephi[nml]/F");
//    T->Branch("Etime",&Etime,"Etime/F");


     T->Branch("Evx",&Evx,"Evx[nml]/F");
     T->Branch("Evy",&Evy,"Evy[nml]/F");
     T->Branch("Evz",&Evz,"Evz[nml]/F");
     T->Branch("Evt",&Evy,"Evt[nml]/F");
     T->Branch("Ebeta",&Ebeta,"Ebeta[nml]/F");
     T->Branch("Estat",&Estat,"Estat[nml]/I");
     T->Branch("EorigIndx",&EorigIndx,"EorigIndx[nml]/I");




// ================   Gamma  ===============    
    T->Branch("nmg",&nmg,"nmg/I");
    T->Branch("Gp",&Gp,"Gp[nmg]/F");
    T->Branch("Gpx",&Gpx,"Gpx[nmg]/F");
    T->Branch("Gpy",&Gpy,"Gpy[nmg]/F");
    T->Branch("Gpz",&Gpz,"Gpz[nmg]/F");
    T->Branch("Gtheta",&Gtheta,"Gtheta[nmg]/F");
    T->Branch("Gphi",&Gphi,"Gphi[nmg]/F");
//    T->Branch("Gtime",&Gtime,"Gtime[nmg]/F");
   
    T->Branch("Gvx",&Gvx,"Gvx[nmg]/F");
    T->Branch("Gvy",&Gvy,"Gvy[nmg]/F");
    T->Branch("Gvz",&Gvz,"Gvz[nmg]/F");
    T->Branch("Gvt",&Gvy,"Gvt[nmg]/F");
    T->Branch("Gbeta",&Gbeta,"Gbeta[nmg]/F");
    T->Branch("Gstat",&Gstat,"Gstat[nmg]/I");
    T->Branch("GorigIndx",&GorigIndx,"GorigIndx[nmg]/I");




//=================  
//

     T->Branch("beamQ",&beamQ,"beamQ/F");
     T->Branch("liveTime",&liveTime,"liveTime/F");
     T->Branch("startTime",&startTime,"startTime/F");
     T->Branch("RFTime",&RFTime,"RFTime/F");
     T->Branch("helicity",&helicity,"helicity/I");
     T->Branch("helicityRaw",&helicityRaw,"helicityRaw/I");
     T->Branch("EventNum",&EventNum,"EventNum/L");
     T->Branch("RunNum",&RunNum,"RunNum/L");

 


     // Define Q2, omega, xB, -t etc ...
     T->Branch("Q2",&Q2,"Q2[nml]/F");
     T->Branch("Nu",&Nu,"Nu[nml]/F");
     T->Branch("q",&q,"q[nml]/F");
     T->Branch("qx",&qx,"qx[nml]/F");
     T->Branch("qy",&qy,"qy[nml]/F");
     T->Branch("qz",&qz,"qz[nml]/F");
     T->Branch("W2",&W2,"W2[nml]/F");
     T->Branch("xB",&xB,"xB[nml]/F");
 
        T->Branch("t",&t,"t[nmb]/F");

 /* 
T->Branch("combint",&combint,"combint/I");
T->Branch("meG",&meG,"meG[combint]/F");
T->Branch("meP", &meP,"meP[combint]/F");
T->Branch("me", &me,"me[combint]/F");
T->Branch("tE",&tE,"tE[combint]/F");
T->Branch("mmG",&mmG,"mmG[combint]/F");
T->Branch("mm",&mm,"mm[combint]/F");
T->Branch("mmP",&mmP,"mmP[combint]/F");
T->Branch("Mpx",&Mpx,"Mpx[combint]/F");
T->Branch("Mpy",&Mpy,"Mpy[combint]/F");
T->Branch("Mpz",&Mpz,"Mpz[combint]/F");
T->Branch("Mp",&Mp,"Mp[combint]/F");
T->Branch("mGpx",&mGpx,"mGpx[combint]/F");
T->Branch("mGpy",&mGpy,"mGpy[combint]/F");
T->Branch("mGpz",&mGpz,"mGpz[combint]/F");
T->Branch("mGp",&mGp,"mGp[combint]/F");
T->Branch("pIndex",&pIndex,"pIndex[combint]/I");
T->Branch("gIndex",&gIndex,"gIndex[combint]/I");
    T->Branch("trento",&trento,"trento[combint]/F");
    T->Branch("trento2",&trento2,"trento[combint]/F");
    T->Branch("theta1",&theta1,"theta1[combint]/F");
    T->Branch("theta2",&theta2,"theta2[combint]/F");

*/
//  =====  Scintillator branching ======

    /* T->Branch("ScInd",&vScInd);
      T->Branch("ScPnd",&vScPnd);
      T->Branch("ScDet",&vScDet);
      T->Branch("ScSet",&vScSet);
      T->Branch("ScLay",&vScLay);
      T->Branch("ScCom",&vScCom);
      T->Branch("ScEne",&vScEne);
      T->Branch("ScTim",&vScTim);
      T->Branch("ScPat",&vScPat);
      T->Branch("ScX",&vScX);
      T->Branch("ScY",&vScY);
      T->Branch("ScZ",&vScZ);
      T->Branch("ScStat",&vScStat);

*/






 
    TLorentzVector vgs;
    TLorentzVector vmP;
    TLorentzVector vmG;
    TLorentzVector vM;



    TVector3 v1;
    TVector3 v2;
    TVector3 Vlept;
    TVector3 Vhadr;
    TVector3 Vhadr2;
    TVector3 vtemp1;
    TVector3 vtemp2;
    TVector3 v3l;
    TVector3 v3h;
    
    
	long index=0;
    
    //
    //loop over fiees
    //
    for(int ifile=0; ifile<chain.GetNFiles();++ifile){
        clas12::clas12reader c12{chain.GetFileName(ifile).Data()};


//  Event bank
//

	auto idx_RECEv = c12.addBank("REC::Event");
	auto aBeamQ = c12.getBankOrder(idx_RECEv,"beamCharge");
	auto aLiveT = c12.getBankOrder(idx_RECEv,"liveTime");
	auto aStarT = c12.getBankOrder(idx_RECEv,"startTime");
	auto aRFTim = c12.getBankOrder(idx_RECEv,"RFTime");
	auto aHelic = c12.getBankOrder(idx_RECEv,"helicity");
	auto aHeRaw = c12.getBankOrder(idx_RECEv,"helicityRaw");

//  Config Bank

	auto idx_RUNCon = c12.addBank("RUN::config");
	auto brun = c12.getBankOrder(idx_RUNCon,"run");
	auto bevent = c12.getBankOrder(idx_RUNCon,"event");




// main particle bank ========
	auto idx_RECPart = c12.addBank("REC::Particle");
   	auto iPid = c12.getBankOrder(idx_RECPart,"pid");
    	auto iPx  = c12.getBankOrder(idx_RECPart,"px");
    	auto iPy  = c12.getBankOrder(idx_RECPart,"py");
   	auto iPz  = c12.getBankOrder(idx_RECPart,"pz");
	auto iVx  = c12.getBankOrder(idx_RECPart,"vx");
	auto iVy  = c12.getBankOrder(idx_RECPart,"vy");
	auto iVz  = c12.getBankOrder(idx_RECPart,"vz");
	auto iVt  = c12.getBankOrder(idx_RECPart,"vt");
	auto iCh  = c12.getBankOrder(idx_RECPart,"charge");
	auto iB  = c12.getBankOrder(idx_RECPart,"beta");
        auto iStat = c12.getBankOrder(idx_RECPart,"status");
//===================


//  Filter bank created by Sangbaek
//	auto idx_FILTER = c12.addBank("FILTER::Index");
//	auto iInd = c12.getBankOrder(idx_FILTER,"before");
//=========


// Read banks: with DC, CVT, FTOF, LTCC, HTCC, ECAL, CTOF, CND 
/*	auto idx_Traj = c12.addBank("REC::Traj");
	auto iPindex = c12.getBankOrder(idx_Traj,"pindex");
	auto iDetector = c12.getBankOrder(idx_Traj,"detector");
	auto iLayer = c12.getBankOrder(idx_Traj,"layer");
	auto iX = c12.getBankOrder(idx_Traj,"x");
        auto iY = c12.getBankOrder(idx_Traj,"y");
        auto iZ = c12.getBankOrder(idx_Traj,"z");
// ========================
*/

// Scintillator bank
/*
	auto idx_RECScint = c12.addBank("REC::Scintillator");
	auto jInd = c12.getBankOrder(idx_RECScint,"index");
	auto jPnd = c12.getBankOrder(idx_RECScint,"pindex");
	auto jDet = c12.getBankOrder(idx_RECScint,"detector");
	auto jSec = c12.getBankOrder(idx_RECScint,"sector");
	auto jLay = c12.getBankOrder(idx_RECScint,"layer");
	auto jCom = c12.getBankOrder(idx_RECScint,"component");
	auto jEne = c12.getBankOrder(idx_RECScint,"energy");
	auto jTim = c12.getBankOrder(idx_RECScint,"time");
	auto jPat = c12.getBankOrder(idx_RECScint,"path");
	auto jX   = c12.getBankOrder(idx_RECScint,"x");
	auto jY = c12.getBankOrder(idx_RECScint,"y");
	auto jZ = c12.getBankOrder(idx_RECScint,"z");
	auto jStat = c12.getBankOrder(idx_RECScint,"status");

*/

// not working properly when don't have all banks.  All detectors related cuts should be already applied. 
   //     c12.addExactPid(11,1);	//exactly 1 electron
  //      c12.addExactPid(2212,1);	//exactly 1 proton
  //        c12.addExactPid(22,1);      //exactly 1 gammas
//===============

        index=0;
        while(c12.next() == true){
		
	        nmb=0;
        	nmg=0;
		nml=0;

		vScInd.clear();
		vScPnd.clear();
		vScDet.clear();
		vScSet.clear();
		vScLay.clear();
		vScCom.clear();
		vScEne.clear();
		vScTim.clear();
		vScPat.clear();
		vScX.clear();
		vScY.clear();
		vScZ.clear();
		vScStat.clear();

               // for(auto ipa = 0;ipa<c12.getBank(idx_FILTER)->getRows();ipa++){
               //         auto val = c12.getBank(idx_FILTER)->getInt(iInd,ipa);
               //         Before[ipa] = val;
               // }

            	for(auto ipa=0;ipa<c12.getBank(idx_RECPart)->getRows();ipa++){
                
                	auto tPx = c12.getBank(idx_RECPart)->getFloat(iPx,ipa);
                	auto tPy = c12.getBank(idx_RECPart)->getFloat(iPy,ipa);
                	auto tPz = c12.getBank(idx_RECPart)->getFloat(iPz,ipa);
			auto tVx = c12.getBank(idx_RECPart)->getFloat(iVx,ipa); 
			auto tVy = c12.getBank(idx_RECPart)->getFloat(iVy,ipa);
			auto tVz = c12.getBank(idx_RECPart)->getFloat(iVz,ipa);
			auto tVt = c12.getBank(idx_RECPart)->getFloat(iVy,ipa);
			auto tB = c12.getBank(idx_RECPart)->getFloat(iB,ipa);
			auto tStat = c12.getBank(idx_RECPart)->getInt(iStat,ipa);

 
                	if( (c12.getBank(idx_RECPart)->getInt(iPid,ipa)) == 11  ){  // electrons
                    		p4_electron[nml].SetXYZM(tPx,tPy,tPz,0.00051);
				Ep[nml] = p4_electron[nml].P();
				Epx[nml] = p4_electron[nml].Px();
				Epy[nml] = p4_electron[nml].Py();
				Epz[nml] = p4_electron[nml].Pz();
				Etheta[nml] = TMath::ATan2( TMath::Sqrt(tPx*tPx + tPy*tPy), tPz);
				Ephi[nml]   = TMath::ATan2(Epy[nml],Epx[nml]);
				Evx[nml] = tVx;
				Evy[nml] = tVy;
				Evz[nml] = tVz;
				Evt[nml] = tVt;
				Ebeta[nml] = tB;

//				EorigIndx[nml] = Before[ipa];  // Original row index before skiming 

				nml++;
                	}
                
	                if((c12.getBank(idx_RECPart)->getInt(iPid,ipa)) == 2212  ){  // protons
             
                	    	p4_proton[nmb].SetXYZM(tPx,tPy,tPz,0.938);
        	            	Pp[nmb] = p4_proton[nmb].P();
                    		Ppx[nmb] = p4_proton[nmb].Px();
                    		Ppy[nmb] = p4_proton[nmb].Py();
                    		Ppz[nmb] = p4_proton[nmb].Pz();
                    		Ptheta[nmb] = TMath::ATan2( TMath::Sqrt(tPx*tPx + tPy*tPy), tPz);
                    		Pphi[nmb]   = TMath::ATan2(tPy,tPx);
				Pvx[nmb] = tVx;
				Pvy[nmb] = tVy;
				Pvz[nmb] = tVz;
				Pvt[nmb] = tVt;
				Pbeta[nmb] = tB;
				Pstat[nmb] = tStat;
		    		int status = tStat;

//                                PorigIndx[nmb] = Before[ipa];  // Original row index before skiming 


//  section to extract correct row from traj after filltering ====
// 
/*			    	if(status<4000){
                    			for(auto ipa1 = 0; ipa1<c12.getBank(idx_Traj)->getRows();ipa1++){
                       				auto val1 = c12.getBank(idx_Traj)->getInt(iPindex,ipa1);
               	        			auto val2 = c12.getBank(idx_Traj)->getInt(iDetector,ipa1);
       	               	 			auto val3 = c12.getBank(idx_Traj)->getInt(iLayer,ipa1);
                                		float valX = c12.getBank(idx_Traj)->getFloat(iX,ipa1);
                                		float valY = c12.getBank(idx_Traj)->getFloat(iY,ipa1);
						float valZ = c12.getBank(idx_Traj)->getFloat(iZ,ipa1);

						if(val2 == 6) { // dc detector
							if(val1 == Before[ipa]){ // same particle index
								if(val3 == 6 ){
									PdcX[nmb][0] = valX;
                                                        		PdcY[nmb][0] = valY;
                                                        		PdcZ[nmb][0] = valZ;
								}
                                                		if(val3 == 18 ){
                                                       	 		PdcX[nmb][1] = valX;
                                                        		PdcY[nmb][1] = valY;
                                                        		PdcZ[nmb][1] = valZ;
                                                		}
                                                		if(val3 == 36 ){
                                                       			PdcX[nmb][2] = valX;
                                                        		PdcY[nmb][2] = valY;
                                                        		PdcZ[nmb][2] = valZ;
                                                		}
							}
						}
   	            			}
		    		}	*/


                   		nmb++;
                    
                	} // if for protons
                    
                	if((c12.getBank(idx_RECPart)->getInt(iPid,ipa)) == 22  ){  // photons

                    		p4_gamma[nmg].SetXYZM(tPx,tPy,tPz,0);
                    		Gp[nmg] = p4_gamma[nmg].P();
                    		Gpx[nmg] = p4_gamma[nmg].Px();
                    		Gpy[nmg] = p4_gamma[nmg].Py();
                    		Gpz[nmg] = p4_gamma[nmg].Pz();
                    		Gtheta[nmg] = TMath::ATan2( TMath::Sqrt(tPx*tPx + tPy*tPy), tPz);
                    		Gphi[nmg]   = TMath::ATan2(tPy,tPx);
                                Gvx[nmg] = tVx;
                                Gvy[nmg] = tVy;
                                Gvz[nmg] = tVz;
                                Gvt[nmg] = tVt;
                                Gbeta[nmg] = tB;
                                Gstat[nmg] = tStat;

//                                GorigIndx[nmg] = Before[ipa];  // Original row index before skiming 


                    		nmg++;
                    
                	}
            	}



// Scintillaror Bank 		//
/*		
		for(auto ipa1 = 0; ipa1<c12.getBank(idx_RECScint)->getRows();ipa1++){

			auto tempInd = c12.getBank(idx_RECScint)->getInt(jInd,ipa1);
			auto tempPnd = c12.getBank(idx_RECScint)->getInt(jPnd,ipa1);
			auto tempDet = c12.getBank(idx_RECScint)->getInt(jDet,ipa1); 	
			auto tempSec = c12.getBank(idx_RECScint)->getInt(jSec,ipa1); 
			auto tempLay = c12.getBank(idx_RECScint)->getInt(jLay,ipa1); 
			auto tempCom = c12.getBank(idx_RECScint)->getInt(jCom,ipa1); 
			auto tempEne = c12.getBank(idx_RECScint)->getFloat(jEne,ipa1);
			auto tempTim = c12.getBank(idx_RECScint)->getFloat(jTim,ipa1); 
			auto tempPat = c12.getBank(idx_RECScint)->getFloat(jPat,ipa1); 
			auto tempX= c12.getBank(idx_RECScint)->getFloat(jX,ipa1); 
			auto tempY = c12.getBank(idx_RECScint)->getFloat(jY,ipa1); 
			auto tempZ = c12.getBank(idx_RECScint)->getFloat(jZ,ipa1);
			auto tempStat = c12.getBank(idx_RECScint)->getInt(jStat,ipa1);

                        vScInd.push_back(tempInd);
			vScPnd.push_back(tempPnd);
			vScDet.push_back(tempDet);
			vScSet.push_back(tempSec);
			vScLay.push_back(tempLay);
			vScCom.push_back(tempCom);
			vScEne.push_back(tempEne);
			vScTim.push_back(tempTim);
			vScPat.push_back(tempPat);
			vScX.push_back(tempX);
			vScY.push_back(tempY);
			vScZ.push_back(tempZ);
			vScStat.push_back(tempStat);
		}// scintillator bank 


*/
    
// event bank ====
//
		for(auto ipa1 = 0; ipa1<c12.getBank(idx_RECEv)->getRows();ipa1++){
			auto tempB = c12.getBank(idx_RECEv)->getFloat(aBeamQ,ipa1);
			auto tempL = c12.getBank(idx_RECEv)->getDouble(aLiveT,ipa1);
			auto tempS = c12.getBank(idx_RECEv)->getFloat(aStarT,ipa1);
			auto tempR = c12.getBank(idx_RECEv)->getFloat(aRFTim,ipa1);
			auto tempH = c12.getBank(idx_RECEv)->getInt(aHelic,ipa1);
			auto tempHR = c12.getBank(idx_RECEv)->getInt(aHeRaw,ipa1);

			beamQ = tempB;
			liveTime = tempL;
			startTime = tempS;
			RFTime = tempR;
			helicity = tempH;
			helicityRaw = tempHR;
		}


// Run config bank
		for(auto ipa1 = 0; ipa1<c12.getBank(idx_RUNCon)->getRows();ipa1++){
			auto tempR = c12.getBank(idx_RUNCon)->getInt(brun,ipa1);
			auto tempE = c12.getBank(idx_RUNCon)->getInt(bevent,ipa1);
			
			EventNum = tempE;
			RunNum = tempR;
		}



	
            /*	if(nml == 1){
            		vgs = p4_beam - p4_electron;
            		Q2 = -vgs.M2();
            		Nu = vgs.E();
            		xB = Q2/(2*0.938*Nu);
               	}
	    */
		for(int i=0;i<nml;i++){
			vgs = p4_beam - p4_electron[i];
			Q2[i] = -vgs.M2();
			Nu[i] = vgs.E();
			xB[i] = Q2[i]/(2*0.938*Nu[i]);
			W2[i] = (p4_beam + p4_target - p4_electron[i]).M2();
			qx[i] = vgs.X();
			qy[i] = vgs.Y();
			qz[i] = vgs.Z();
		        q[i] = TMath::Sqrt(qx[i]*qx[i] + qy[i]*qy[i] + qz[i]*qz[i]);
	
		}
		for(int i=0;i<nmb;i++){
			t[i] = 2*0.938*(p4_proton[i].E() - 0.938);

		}
		
		combint = 0;
		for(int i=0;i<nmb;i++){
			vgs = p4_beam - p4_electron[0];

			for(int j=0;j<nmg;j++){
			
	                        auto temp1 = vgs.Px()*p4_gamma[j].Px() + vgs.Py()*p4_gamma[j].Py() + vgs.Pz()*p4_gamma[j].Pz();
        	                auto temp2 = TMath::Sqrt( vgs.Px() * vgs.Px() + vgs.Py() * vgs.Py() + vgs.Pz() * vgs.Pz());
                	        auto temp3 = TMath::Sqrt( p4_gamma[j].Px() * p4_gamma[j].Px() + p4_gamma[j].Py() * p4_gamma[j].Py() + p4_gamma[j].Pz() * p4_gamma[j].Pz());
                        	auto costheta = temp1/(temp2 * temp3);

		
        	                tE[combint] =   (mp*Q2[0] + 2 *mp*Nu[0]*( Nu[0] - TMath::Sqrt(Nu[0]*Nu[0] + Q2[0]) *costheta ))/(mp + Nu[0] - TMath::Sqrt(Nu[0]*Nu[0] + Q2[0])*costheta);
	                        vmP = p4_beam + p4_target - p4_electron[0] - p4_gamma[j];
        	                vmG = p4_beam + p4_target - p4_electron[0] - p4_proton[i];

                	        vM = p4_beam + p4_target - p4_electron[0] - p4_proton[i] - p4_gamma[j];
                	        mmG[combint] = vmG.M2();
                        	meG[combint] = vmG.E();
	                        mmP[combint] = vmP.M2();
        	                meP[combint] = vmP.E();
                	        mm[combint] = vM.M2();
                        	me[combint] = vM.E();


	                        mGpx[combint] = vmG.Px();
        	                mGpy[combint] = vmG.Py();
                	        mGpz[combint] = vmG.Pz();
                        	mGp[combint]  = vmG.P();

	                        Mpx[combint] = vM.Px();
        	                Mpy[combint] = vM.Py();
                	        Mpz[combint] = vM.Pz();
                        	Mp[combint]  = vM.P();

				pIndex[combint] = i;
				gIndex[combint] = j;

	


				v1.SetXYZ(p4_gamma[j].Px(),p4_gamma[j].Py(),p4_gamma[j].Pz());
	                	v2.SetXYZ(vmG.Px(),vmG.Py(),vmG.Pz());
	        	        theta1[combint] = TMath::ACos(v1.Dot(v2)/(v1.Mag()*v2.Mag()) )*180./TMath::Pi(); // from Sangbaek code


				v2.SetXYZ(vgs.Px(),vgs.Py(),vgs.Pz());
				v1.SetXYZ(p4_proton[i].Px(),p4_proton[i].Py(),p4_proton[i].Pz());
                    		Vhadr = v1.Cross(v2);

                    		v1.SetXYZ(p4_gamma[j].Px(),p4_gamma[j].Py(),p4_gamma[j].Pz());
                    		Vhadr2 = v2.Cross(v1);


			// trento 1
                                v1.SetXYZ(p4_beam.Px(),p4_beam.Py(),p4_beam.Pz());
                                v2.SetXYZ(p4_electron[0].Px(),p4_electron[0].Py(),p4_electron[0].Pz());
                                v3l = v1.Cross(v2);

                                v1.SetXYZ(p4_proton[i].Px(),p4_proton[i].Py(),p4_proton[i].Pz());
                                v2.SetXYZ(vgs.Px(),vgs.Py(),vgs.Pz());
                                v3h = v1.Cross(v2);

                                if( v1.Mag() * v2.Mag() !=0 && v1.Dot(v2)<v1.Mag()*v2.Mag() ) {
                                    trento[combint] = TMath::ACos(v3l.Dot(v3h)/(v3l.Mag()*v3h.Mag()) ) * 180./TMath::Pi();
                                }
                                else trento[combint] =0;

                                if(v3l.Dot(v1) >0 ) trento[combint] = 360 - trento[combint];

			// trento 2
                                v2.SetXYZ(p4_electron[0].Px(),p4_electron[0].Py(),p4_electron[0].Pz());
                                v1.SetXYZ(vgs.Px(),vgs.Py(),vgs.Pz());

                                vtemp1 = v1.Cross(v2);
                                v2.SetXYZ(p4_gamma[j].Px(),p4_gamma[j].Py(),p4_gamma[j].Pz());

                                vtemp2 = v1.Cross(v2);

                                if( vtemp1.Mag() * vtemp2.Mag() !=0 && vtemp1.Dot(vtemp2)<vtemp1.Mag()*vtemp2.Mag() ) {
                                    trento2[combint] = TMath::ACos(vtemp1.Dot(vtemp2)/(vtemp1.Mag()*vtemp2.Mag()) ) * 180./TMath::Pi();
                                }
                                else trento2[combint] =0;

                                if (v1.Dot(vtemp1.Cross(vtemp2))<0) trento2[combint] = 360 - trento2[combint];



				combint++;


			}
		}


/*
		if( nml==1 && nmb ==1 && nmg ==1){
			vgs = p4_beam - p4_electron[0];
	                auto temp1 = vgs.Px()*p4_gamma[0].Px() + vgs.Py()*p4_gamma[0].Py() + vgs.Pz()*p4_gamma[0].Pz();
        	        auto temp2 = TMath::Sqrt( vgs.Px() * vgs.Px() + vgs.Py() * vgs.Py() + vgs.Pz() * vgs.Pz());
                	auto temp3 = TMath::Sqrt( p4_gamma[0].Px() * p4_gamma[0].Px() + p4_gamma[0].Py() * p4_gamma[0].Py() + p4_gamma[0].Pz() * p4_gamma[0].Pz());

	                auto costheta = temp1/(temp2 * temp3);

	                tE =   (mp*Q2[0] + 2 *mp*Nu[0]*( Nu[0] - TMath::Sqrt(Nu[0]*Nu[0] + Q2[0]) *costheta ))/(mp + Nu[0] - TMath::Sqrt(Nu[0]*Nu[0] + Q2[0])*costheta);


        	        vmP = p4_beam + p4_target - p4_electron[0] - p4_gamma[0];
                	vmG = p4_beam + p4_target - p4_electron[0] - p4_proton[0];

	                vM = p4_beam + p4_target - p4_electron[0] - p4_proton[0] - p4_gamma[0];

        	        mmG = vmG.M2();
			meG = vmG.E();
                	mmP = vmP.M2();
			meP = vmP.E();
	                mm = vM.M2();
			me = vM.E();
			

			mGpx = vmG.Px();
			mGpy = vmG.Py();
			mGpz = vmG.Pz();
			mGp  = vmG.P();
			
			Mpx = vM.Px();
			Mpy = vM.Py();
			Mpz = vM.Pz();
			Mp  = vM.P();


		}
        	    else{
			meG  = -100;
			meP  = -100;
			me   = -100;
                	tE   = -100;
	                mmG  = -100	;
        	        mm   =  -100;
                	mmP  =-100;
			Mpx  = -100;
			Mpy  = -100;
			Mpz  = -100;
			Mp   = -100;
                        mGpx = -100;
                        mGpy = -100;
                        mGpz = -100;
                        mGp  = -100;
            	}

*/

		if(nml==1 && nmg>1)
            	T->Fill();

        }

    }

	rFile->Write();
	rFile->Close();

return 1;
}
