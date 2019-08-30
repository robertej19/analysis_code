#!/usr/bin/python

import sys
sys.argv.append('-b')
import ROOT

ff = ROOT.TFile(sys.argv[1])

h1 = ff.Get("5038_Hist_deltaB_psec5_layer2")#.ProjectionY("cutg",100,140,"[cutg]")


c1 = ROOT.TCanvas('c1','c1',1100,800)
c1.SetLogz()
h1.Draw("colz")
c1.Print("protons.pdf")
