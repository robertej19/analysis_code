#!/usr/bin/python

import sys
sys.argv.append('-b')
import ROOT

ff = ROOT.TFile(sys.argv[1])
#f1 = ROOT.TF1('f1', 'gaus(0)+pol0(3)', -0.15,.15)
#f1 = ROOT.TF1('f1', 'gaus',-0.1,0.1)
#print(ff)
#print(type(ff))

#for kk in ff.GetListOfKeys():
#  obj = kk.ReadObj()
#  print(obj.__class__)
#  print(obj.GetTitle())
#  print(obj.GetNbinsX())
#  print(obj.GetNbinsY())



h1 = ff.Get("5038_H_proton_DeltaBeta_momentum_S2").ProjectionY("cutg",100,140,"[cutg]")
#print(type(h1))
#print("h is")
#print(h1)
#f1.SetParameter(1, h1.GetBinCenter(h1.GetMaximumBin()))
#f1.SetParameter(2, h1.GetRMS())
#mu,sig = [f1.GetParameter(i) for i in range(1,3)]

#f1.SetRange(mu-2.5*abs(sig), mu+2.5*abs(sig))
#h1.Fit(f1)
mu,sig = [f1.GetParameter(i) for i in range(1,3)]

#f1.SetRange(mu-2.5*abs(sig), mu+2.5*abs(sig))
h1.Fit(f1, 'R')

qq = ROOT.TSpectrum(2*3)
nfound = qq.Search(h1,1,"new")
print("Found the following {0}".format(nfound))
#mu,sig = [f1.GetParameter(i) for i in range(1,3)]
#print("mu is".format(mu))
#print("sig is".format(sig))
print("param is:")
print(f1.GetParameter(0))
print(f1.GetParameter(1))
print(f1.GetParameter(2))
print(f1.GetParameter(3))
#iTF1 *fit = new TF1("fit",fpeaks,0,1000,2+3*npeaks);
 #  TVirtualFitter::Fitter(h2,10+3*npeaks); //we may have more than the default 25 parameters
#   fit->SetParameters(par);
#   fit->SetNpx(1000);
#   h2->Fit("fit");


#c1.Update()
#c1.cd(2)
#   TSpectrum *s = new TSpectrum(2*npeaks);
 #  Int_t nfound = s->Search(h,1,"new");
  # printf("Found %d candidate peaks to fitn",nfound);
  # c1->Update();
  # c1->cd(2);




c1 = ROOT.TCanvas('c1','c1',1100,800)
h1.Draw("colz")
c1.Print("protons.pdf")
