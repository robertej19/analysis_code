#!/usr/bin/python

import sys
sys.argv.append('-b') #This is for root batch mode, so that no canvas windows open up
import subprocess
import os
import ROOT
from ROOT import gStyle
from ROOT import gROOT
from ROOT import TStyle
from pathlib import Path
import json
import array, numpy


"""
python2 2gen_all_plots.py ../../analysis_outputs/20201011/output_file_histos-20201011-17-56/output_file_histos-20201011-17-56.hipo.root ../../analysis_outputs/20201013/output_file_histos-20201013-15-04/output_file_histos-20201013-15-04.hipo.root

"""




with open('../../histogram_dict.json') as f:
  data = json.load(f)



rootfilename = sys.argv[1]
root_tree = ROOT.TFile(rootfilename)
root_fileID = ((rootfilename.split("/"))[-1]).split(".")[0] #e.g. this is something like "output_file_histos23-33"



rootfilename_2 = sys.argv[2]
root_tree_2 = ROOT.TFile(rootfilename_2)
root_fileID_2 = ((rootfilename_2.split("/"))[-1]).split(".")[0] #e.g. this is something like "output_file_histos23-33"

"""
for key in root_tree.GetListOfKeys():
  obj = key.ReadObj()
  print(obj.GetName())
  print(obj.GetTitle())
  print(obj.GetNbinsX())
  print(obj.GetNbinsY())
  print(obj.GetEntries())
"""



def makeplot(plots_dir,hist_root_ID,hist_name, display_title, num_bins_x, x_bin_min, x_bin_max,
				x_axis_title, y_axis_title, y_scale_max,double_plots, second_histo_root_ID, log_scale):

	ROOT.gErrorIgnoreLevel = ROOT.kWarning #This quiets root file save messages

	h1 = root_tree.Get(hist_root_ID)
	
	print("hist root id is 1 {}".format(hist_root_ID))
	c1 = ROOT.TCanvas('c1','c1',120,100)

	#need to get this fixed, currently only works with int arguements
	#if x_bin_max > 0:
	
	h1.Draw("colz")
	if y_scale_max > 0:
		h1.SetAxisRange(0,y_scale_max,"Y")

	gStyle.SetOptStat(0)
	h1.SetTitle(display_title)
	h1.SetXTitle(x_axis_title)
	h1.SetYTitle(y_axis_title)

	h1.GetXaxis().CenterTitle()
	h1.GetYaxis().CenterTitle()

	rebinfact = 1
	h1.Rebin(rebinfact)

	
	#h1.GetXaxis().SetTitleSize(5)
	#gStyle.SetLabelSize(50)
	h1.SetLineWidth(2) #use this to make line width thicker

#	binmax = h1.GetMaximumBin()
	max_yval1 = h1.GetMaximum()




	y1int = h1.Integral()

	print("maxint n is {}".format(y1int))

	print("max bin is {}".format(max_yval1))
	print("Saving Histogram {}".format(h1.GetName()))

	#c1.Print(".{}/{}.pdf".format(plots_dir,hist_name))




	

	"""

			# Make two graphs with the same X ranges but different Y values.
			x = array.array('d',range(10))
			y1 = array.array('d',(i for i in x))
			y2 = array.array('d',(-3*i-5 for i in x))

			g1 = ROOT.TGraph(len(x),x,y1)
			g2 = ROOT.TGraph(len(x),x,y2)
			color2 = ROOT.kBlue
			g2.SetLineColor(color2)
			g2.SetMarkerColor(color2)
			g2.SetLineStyle(2)

			# Get the dynamic range of both graphs.
			# Note: you cannot use GetMaximum() or GetMaximum()
			# as these can contain magic codes like -1111 for autoscaling.
			y1max = ROOT.TMath.MaxElement(g1.GetN(),g1.GetY())
			y1min = ROOT.TMath.MinElement(g1.GetN(),g1.GetY())
			y2max = ROOT.TMath.MaxElement(g2.GetN(),g2.GetY())
			y2min = ROOT.TMath.MinElement(g2.GetN(),g2.GetY())

			# Define a TF2 that takes a y2 value and scales it to a new
			# y3 value that has the same dynamic range as y1.
			params = [ ("y1max", y1max), ("y1min", y1min),
					("y2max", y2max), ("y2min", y2min) ]
			fscale = ROOT.TF2("fscale","(y-[3])*([1]-[0])/([3]-[2]) + [1]",y1min,y1max,y2min,y2max)
			for i,(parname, parvar) in enumerate(params):
				fscale.SetParName(i,parname)
				fscale.SetParameter(parname,parvar)

			# Make a new copy of g2 so that the original data are preserved.
			g3 = g2.Clone("g3")
			g3.Apply(fscale)

			# Sanity check
			y3max = ROOT.TMath.MaxElement(g3.GetN(),g3.GetY())
			y3min = ROOT.TMath.MinElement(g3.GetN(),g3.GetY())
			assert (y3max == y1max) and (y3min == y1min)

			# Now draw only the first graph and the scaled one.
			mg = ROOT.TMultiGraph("mg","mg")
			mg.Add(g1)
			mg.Add(g3)
			mg.Draw("ALP")
			mg.GetXaxis().SetTitle("g1")
			ROOT.gPad.Modified()
			ROOT.gPad.Update()

			# Get the coordinates of the left-side Y axis.
			uxmax, uymin, uymax = ROOT.gPad.GetUxmax(), ROOT.gPad.GetUymin(), ROOT.gPad.GetUymax()

			# Define the inverse function of fscale
			funscale = ROOT.TF2("funscale","(y-[0])*([3]-[2])/([1]-[0])+[2]",y1min,y1max,y3min,y3max)
			for i,(parname, parvar) in enumerate(params):
				funscale.SetParName(i,parname)
				funscale.SetParameter(parname,parvar)
			# Note: funscale(0,fscale(0,i)) == i should always be true for all real numbers i.

			right_axis = ROOT.TGaxis(uxmax, uymin, uxmax, uymax,
									funscale(0,uymin), funscale(0,uymax), 510, "+L" )
			right_axis.SetTitle("g2")
			right_axis.Draw()

"""	


	if double_plots == "yes":
		print("accessing title {}".format("output_file_histos_"+second_histo_root_ID))
		
		print("hist root id is 2 {}".format("output_file_histos_"+second_histo_root_ID))

		h2 = root_tree_2.Get("{}".format("output_file_histos_"+second_histo_root_ID))
		print(h2.GetName())

		max_yval2 = h2.GetMaximum()

		y2int = h2.Integral()

		scalefactor_natural = max_yval1/max_yval2
		scalefactor_excuts =  218463/84239
		
		scalefactor_all = 215227870/355718

		if "fter" in display_title:
			scalefactor = scalefactor_excuts
		else:
			scalefactor = scalefactor_all

		if scalefactor*max_yval2 > max_yval1:
			scalefactor = scalefactor_natural


		scalefactor = y1int/y2int

		h2.Rebin(rebinfact)

		h2.Scale(scalefactor,"height")
		#h2.SetFillStyle(3335)
		h2.SetFillStyle(3004)
		h2.SetFillColor( 42)

		h2.SetLineColorAlpha(2,1)
		h2.Draw("HIST SAME")

		#h2.SetAxisRange(0,110000,"Y")
		display_title += " + Scaled Sim."


		#xmins = 400
		#xmaxs = 600
		xmins = 0
		xmaxs = 180
		
		h2.GetXaxis().SetRange(xmins,xmaxs)
		h1.GetXaxis().SetRange(xmins,xmaxs)

		gStyle.SetOptStat(0)
		h1.SetTitle(display_title)
		h1.SetXTitle(x_axis_title)
		h1.SetYTitle(y_axis_title)
		h1.SetLineWidth(3) #use this to make line width thicker

		c1.Print(".{}/{}.pdf".format(plots_dir,hist_name+"_Double"))


	if log_scale == "yes":
		c1.SetLogz()
		c1.Print(".{}/{}.pdf".format(plots_dir,hist_name+"_logON"))


#plots_dir = "/../../analysis_outputs/{}/plots/original_python_pdfs".format(root_fileID)
plots_dir = "/testplots2"
plots_folder= os.path.dirname(os.path.abspath(__file__))+plots_dir



if os.path.isdir(plots_folder):
	print('removing previous database file')
	subprocess.call(['rm','-rf',plots_folder])
else:
	print(plots_folder+" is not present, not deleteing")

subprocess.call(['mkdir','-p',plots_folder])
print(plots_folder+" is now present")





hist_suffexes_cuts = ["_nocut","_prexcut","_excut"]
title_suffexes_cuts = [", No Cuts ",", Before Excl. Cuts",", After Excl. Cuts"]
hist_suffexes_locs = ["all","fd","cd"]
title_suffexes_locs = [", FD & CD",", FD",", CD"]


def title_maker(data_dict,hist_name):
	title_base = data[hist_key][0]["display_title"]

	for cut_ind, suffex_cut in enumerate(hist_suffexes_cuts):
		if suffex_cut in hist_name:
			title_base += title_suffexes_cuts[cut_ind]

	for loc_ind, suffex_loc in enumerate(hist_suffexes_locs):
		if suffex_loc in hist_name:
			title_base += title_suffexes_locs[loc_ind]

	#print("title for {} is {}".format(hist_name, title_base))
	return title_base

	


hist_file_title_mapping = {}

for key in root_tree.GetListOfKeys():
	obj = key.ReadObj()
	hist_root_ID = obj.GetName() #e.g. this is something like output_file_histos_hist_phi_proton_excuts_FD
	hist_name = hist_root_ID.split("file_histos_")[1] #e.g. this is something like hist_phi_proton_excuts_FD
	#print("hist name is {}".format(hist_name))
	if ("hist_phi_xbq2t" in hist_name) or ("hist_t_xbq2" in hist_name):
			#print("skipping {}, for now".format(hist_name))
			xv = 2
	else:

		keylogs = 0 



		for icut in hist_suffexes_cuts:
			if icut in hist_name:
				hist_rooter = hist_name.split(icut)[0]
		
		print("HIST ROOT IS \n \n \n {} \n\n\n".format(hist_rooter))

		for hist_key in data:
			#print(hist_key)
			
			if data[hist_key][0]["root_title"] == hist_rooter:
				keylogs = 1
			
				

				
				# if hist_name in data:
				# #for key in data[hist_name][0]:
				# #	print("hist property {} has value {}".format(key,data[hist_name][0][key]))

				display_title = title_maker(data,hist_name)
				num_bins_x      = data[hist_key][0]["num_bins_x"]
				x_bin_min 	  = data[hist_key][0]["x_bin_min"]
				x_bin_max     = data[hist_key][0]["x_bin_max"]
				x_axis_title        = data[hist_key][0]["x_axis"]
				y_axis_title        = data[hist_key][0]["y_axis"]
				y_scale_max   = data[hist_key][0]["y_scale_max"]
				double_plots   = "no"
				second_histo_root_title = data[hist_key][0]["second_histo_root_title"]
				log_scale   = data[hist_key][0]["log_scale"]


				if hist_name == "hist_num_protons":
					print("GOT STRANGE PLOT: \n\n\n\n\n\n\n\n\n\n\n\n her:")
					print("display is {}".format(display_title))

				if "CD" not in display_title:
					if second_histo_root_title == "yes":
						print("trying to doubleplot")
						print(second_histo_root_title)

						double_plots   = "yes"
						second_histo_root_title = hist_name

						makeplot(plots_dir,hist_root_ID,hist_name,
									display_title, num_bins_x, x_bin_min, x_bin_max,
									x_axis_title, y_axis_title, y_scale_max,
									double_plots, second_histo_root_title, log_scale)

				hist_file_title_mapping[hist_name+".pdf"] = display_title

		if keylogs == 0:
			print("File {} not found in json formatting, skipping".format(hist_name))


#with open("dict_to_json_textfile.txt", 'w') as fout:
 #   json_dumps_str = json.dumps(hist_file_title_mapping, indent=4)
#    print(json_dumps_str, file=fout)
	

#print(hist_file_title_mapping)

#example_path = Path('dict_to_json_textfile.json')
#json_str = json.dumps(hist_file_title_mapping, indent=4) + '\n'
#example_path.write_text(json_str, encoding='utf-8')

with open('dict_to_json_textfile.json', 'w') as output_file:
        json.dump(hist_file_title_mapping, output_file, indent=4)
