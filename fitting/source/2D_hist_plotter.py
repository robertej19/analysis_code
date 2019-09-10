#!/usr/bin/python

import sys
#I don't think the below line does anything, please delete after confirming this file still works
#sys.argv.append('-b')
"""import ROOT DONT FORGET TO TURN THIS BACK ON"""
import get_args

"""
Pseudo code:

run groovy
args:
returns:

run hipo2root:
args:
returns:

run 2dhists:
args:
returns:

run fitter:
args:
returns:

"""

def plot_hists(args):
	plot_titles = ["_Hist_beta_psec1_layer1","_Hist_deltaB_psec1_layer1","_Hist_beta_p2sec1_layer1",
			"_Hist_beta_p_ctof"]
	plot_title_ending = plot_titles[int(args.plot_type)-1]
	filenameX = args.filename.split("/")[-1].split(".")[0]
	Run = filenameX.split("_")[-1]
	print(Run)
	print(Run+plot_title_ending)

def root_plotting():

	"""
	root_file = ROOT.TFile(args.filename)
	hist = root_file.Get(plot_titles[int(args.plot_type)-1])#.ProjectionY("cutg",0,40,"[cutg]")
	canvas = ROOT.TCanvas('canvas','canvas',1100,800)
	canvas.SetLogz()
	hist.Draw("colz")
	canvas.Print("plots/full_{}_{}.pdf".format(zz,type))
	"""

def get_titles(root_file):
	for key in root_file.GetListOfKeys():
		hist_object = key.ReadObj()
		print(hist_object.__class__)
		print(hist_object.GetTitle())
		print(hist_object.GetNbinsX())
		print(hist_object.GetNbinsY())
		print("value of x bin is: ")
		#print(obj.GetXaxis().FindBin(100))#obj.FindLastBinAbove()))

if __name__ == "__main__":
	args = get_args.get_args()
	print(args.filename)
	plot_hists(args)
