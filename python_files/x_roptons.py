#!/usr/bin/python

import sys
sys.argv.append('-b')
import ROOT

ff = ROOT.TFile(sys.argv[1])
#f1 = ROOT.TF1('f1', 'gaus(0)+pol0(3)', 0,1)
print(ff)
print(type(ff))
h1 = ff.Get("5038_H_proton_DeltaBeta_momentum_S2")#.ProjectionY("cutg",125,150,"[cutg]")
print(type(h1))
print("h is")
print(h1)
#f1.SetParameter(1, h1.GetBinCenter(h1.GetMaximumBin()))
#f1.SetParameter(2, h1.GetRMS())
#mu,sig = [f1.GetParameter(i) for i in range(1,3)]

#f1.SetRange(mu-2.5*abs(sig), mu+2.5*abs(sig))
#h1.Fit(f1)
#mu,sig = [f1.GetParameter(i) for i in range(1,3)]

#f1.SetRange(mu-2.5*abs(sig), mu+2.5*abs(sig))
#h1.Fit(f1, 'R')

c1 = ROOT.TCanvas('c1','c1',1100,800)
h1.Draw("colz")
c1.Print("protons.pdf")
