#!/usr/bin/python

import sys
sys.argv.append('-b') #This is for root batch mode, so that no canvas windows open up
import subprocess
import os
import ROOT
from ROOT import gStyle
from ROOT import gROOT
from ROOT import TStyle
import json

with open('../../histogram_dict.json') as f:
  data = json.load(f)



rootfilename = sys.argv[1]

root_tree = ROOT.TFile(rootfilename)
root_fileID = ((rootfilename.split("/"))[-1]).split(".")[0] #e.g. this is something like "output_file_histos23-33"

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
	print("Saving Histogram {}".format(h1.GetName()))
	c1 = ROOT.TCanvas('c1','c1',120,100)

	#need to get this fixed, currently only works with int arguements
	#if x_bin_max > 0:
	#	h1.GetXaxis().SetRange(x_bin_min,x_bin_max)
	h1.Draw("colz")
	if y_scale_max > 0:
		h1.SetAxisRange(0,y_scale_max,"Y")

	gStyle.SetOptStat(0)
	h1.SetTitle(display_title)
	h1.SetXTitle(x_axis_title)
	h1.SetYTitle(y_axis_title)
	#h1.GetXaxis.SetLabelSize(10)
	gStyle.SetLabelSize(50)
	h1.SetLineWidth(3) #use this to make line width thicker

	c1.Print(".{}/{}.pdf".format(plots_dir,hist_name))


	if double_plots == "yes":
		print("accessing title {}".format("output_file_histos_"+second_histo_root_ID))
		
		h2 = root_tree.Get("{}".format("output_file_histos_"+second_histo_root_ID))
		print(h2.GetName())
		h2.SetLineColorAlpha(2,1)
		h2.Draw("SAME")
		#h2.SetAxisRange(0,110000,"Y")
		display_title += " + Overlay"

		gStyle.SetOptStat(0)
		h1.SetTitle(display_title)
		h1.SetXTitle(x_axis_title)
		h1.SetYTitle(y_axis_title)
		h1.SetLineWidth(3) #use this to make line width thicker

		c1.Print(".{}/{}.pdf".format(plots_dir,hist_name+"_Double"))


	if log_scale == "yes":
		c1.SetLogz()
		c1.Print(".{}/{}.pdf".format(plots_dir,hist_name+"_logON"))


plots_dir = "/../plots/{}/original_python_pdfs".format(root_fileID)
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
title_suffexes_locs = [", All",", FD",", CD"]


def title_maker(data_dict,hist_name):
	title_base = data[hist_key][0]["display_title"]

	for cut_ind, suffex_cut in enumerate(hist_suffexes_cuts):
		if suffex_cut in hist_name:
			title_base += title_suffexes_cuts[cut_ind]

	for loc_ind, suffex_loc in enumerate(hist_suffexes_locs):
		if suffex_loc in hist_name:
			title_base += title_suffexes_locs[loc_ind]

	print("title for {} is {}".format(hist_name, title_base))
	return title_base

	




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
		for hist_key in data:
			#print(hist_key)
			
			if data[hist_key][0]["root_title"] in hist_name:
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

			
				makeplot(plots_dir,hist_root_ID,hist_name,
							display_title, num_bins_x, x_bin_min, x_bin_max,
							x_axis_title, y_axis_title, y_scale_max,
							double_plots, second_histo_root_title, log_scale)

		if keylogs == 0:
			print("File {} not found in json formatting, skipping".format(hist_name))


"""FORMAT: Hist name, title, xaxis, yaxis,logON/LogOff,xmin,xmax,ymax,1 = enable double plots,second histo name"""


Dtype8 = ("output_file_histos_Hist_hproThetaFD",
"Proton Angle (Theta) in FD and in CD","Angle","Counts",
0,0,0,200000,1,"output_file_histos_Hist_hproThetaCD")

Dtype9 = ("output_file_histos_Hist_hproThetaFDaftercuts",
"Proton Angle (Theta) in FD and in CD after Excl. Cuts","Angle","Counts",
0,0,0,300,1,"output_file_histos_Hist_hproThetaCDaftercuts")


"""
	#if "Ultra_Phi" in title:
	#	if obj.GetEntries()>10 and obj.GetMaximum()>10:
	#	 histTitle = title
	#type9 = (title,title,"Phi","Counts",0,0,0,0,0,0)
	
	#plotdistributer(type9,zz,zzz)
"""


# #Full xb range:
# #xbRange = ["0.00", "0.10", "0.20", "0.30","0.40", "0.50", "0.60", "0.70", "0.80"]
# #Full q2 range:
# #q2Range = ["0.0","0.5","1.0","1.5","2.0","2.5","3.0", "3.5","4.0", "4.5","5.0", "5.5","6.0", "6.5", "7.0","7.5","8.0", "8.5"]

# """
# xbRange = ["0.10", "0.20", "0.30","0.40", "0.50", "0.60", "0.70", "0.80"]
# q2Range = ["1.0","1.5","2.0","2.5","3.0", "3.5","4.0", "4.5","5.0", "5.5","6.0"]
# tRange = ["0.09","0.15","0.2","0.3","0.4","0.6","1.0","1.5","2","5"]


# """
# for k in range(0,len(tRange)-1):
# 	for j in range(0,len(q2Range)-1) :
# 		for i in range(0,len(xbRange)-1):
# 			title = "output_file_histos_Hist_Ultra_Phi{} < xB < {}_ {} < q2 < {} {} < t < {}".format(xbRange[i],xbRange[i+1],q2Range[j],q2Range[j+1],tRange[k],tRange[k+1])
# 			histTitle = "Counts vs. Phi, {} < xB < {}_ {} < q2 < {} {} < t < {}".format(xbRange[i],xbRange[i+1],q2Range[j],q2Range[j+1],tRange[k],tRange[k+1])
# 			type9 = (title,histTitle,"Phi","Counts",0,0,0,0,0,0)
# 			print(title)
# 			plotdistributer(type9,zz,zzz)
# """
# """FORMAT: Hist name, title, xaxis, yaxis,logON/LogOrootfilename,xmin,xmax,ymax,1 = enable double plots,second histo name"""

# """
# for j in range(0,len(q2Range)-1) :
# 	for i in range(0,len(xbRange)-1):
# 		title = "output_file_histos_Hist_beta_T{} < xB < {}_ {} < q2 < {}".format(xbRange[i],xbRange[i+1],q2Range[j],q2Range[j+1])
# 		histTitle = "Counts vs. t, {} < xB < {}_ {} < q2 < {}".format(xbRange[i],xbRange[i+1],q2Range[j],q2Range[j+1])
# 		type9 = (title,histTitle,"t (GeV^2)","Counts",0,0,25,1000,0,0)
# 		print(title)
# 		plotdistributer(type9,zz,zzz)
# """
# for type in plots:
# 	plotdistributer(type,zz,zzz)
