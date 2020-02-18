#!/usr/bin/python

import sys
sys.argv.append('-b')
import ROOT

ff = ROOT.TFile(sys.argv[1])


h1 = ff.Get("5038_H_proton_DeltaBeta_momentum_S0")
print(h1)
c1 = ROOT.TCanvas('c1','c1',1100,800)

#h0.Draw()
c1.Print("ebeam.pdf")
