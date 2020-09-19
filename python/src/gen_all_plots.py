#!/usr/bin/python

import sys
import subprocess
import os
import ROOT
from ROOT import gStyle
from ROOT import gROOT
from ROOT import TStyle

rootfilename = sys.argv[1]
typeZZZ = int(sys.argv[2])
LogOn = int(sys.argv[3])


root_tree = ROOT.TFile(rootfilename)
fileID = ((rootfilename.split("/"))[-1]).split(".")[0]

"""
for key in root_tree.GetListOfKeys():
  obj = key.ReadObj()
  print(obj.GetName())
  print(obj.GetTitle())
  print(obj.GetNbinsX())
  print(obj.GetNbinsY())
  print(obj.GetEntries())
"""


def plotdistributer(plots_dir,typeX,zz,zzz):
	makeplot(plots_dir,typeX,"LogOff",zz,zzz)
	if typeX[4]:
		makeplot(plots_dir,typeX,"LogON",zz,zzz)

def makeplot(plots_dir,typeXX,logtitle,zz,zzz):
	#print(type(typeXX))
	hist_title = typeXX[0]
	h1 = root_tree.Get(hist_title)
	c1 = ROOT.TCanvas('c1','c1',120,100)
	if typeXX[6] > 0:
		h1.GetXaxis().SetRange(typeXX[5],typeXX[6])
	h1.Draw("colz")
	if typeXX[7] > 0:
		h1.SetAxisRange(0,typeXX[7],"Y")
	if typeXX[8] > 0:
		h2 = rootfilename.Get(typeXX[9])
		h2.SetLineColorAlpha(2,1)
		h2.Draw("SAME")
		h2.SetAxisRange(0,110000,"Y")
		logtitle += "_Double"

	#gStyle.SetOptStat(0)
	h1.SetTitle(typeXX[1])
	h1.SetXTitle(typeXX[2])
	h1.SetYTitle(typeXX[3])
	#h1.SetLineWidth(5) #use this to make line width thicker

	if logtitle == "LogON":
		c1.SetLogz()
	#c1.Draw()
	c1.Print(".{}/{}_{}.pdf".format(plots_dir,zzz,typeXX[1]))


plots_dir = "/../plots/{}/original_python_pdfs".format(fileID)
plots_folder= os.path.dirname(os.path.abspath(__file__))+plots_dir



if os.path.isdir(plots_folder):
	print('removing previous database file')
	subprocess.call(['rm','-rf',plots_folder])
else:
	print(plots_folder+" is not present, not deleteing")

subprocess.call(['mkdir','-p',plots_folder])
print(plots_folder+" is now present")


"""
title1 = "output_file_histos_hist_xB_excuts"
type9 = (title1,"This is the title","xais","y axis",0,0,0,0,0,0)
plotdistributer(plots_dir,type9,fileID,fileID)

"""

"""
Make a dictionary mappping hist titles to axis titles, other properities
Add date to filename!
"""




for key in root_tree.GetListOfKeys():
	obj = key.ReadObj()
	title_from_root = obj.GetName()
	title_save_file = title_from_root.split("file_histos_hist_")[1]
	
	type9 = (title_from_root,title_save_file,"xais","y axis",0,0,0,0,0,0)
	plotdistributer(plots_dir,type9,fileID,title_save_file)
	#if "Ultra_Phi" in title:
	#	if obj.GetEntries()>10 and obj.GetMaximum()>10:
	#	 histTitle = title
	#type9 = (title,title,"Phi","Counts",0,0,0,0,0,0)
	
	
	#plotdistributer(type9,zz,zzz)



"""FORMAT: Hist name, title, xaxis, yaxis,logON/LogOrootfilename,xmin,xmax,ymax,1 = enable double plots,second histo name"""
type1 = ("output_file_histos_Hist_heleproTheta",
"Proton vs. Electron Theta Angle","Proton Angle","Electron Angle",
1,0,0,0,0,0)
type2 = ("output_file_histos_Hist_heleproThetaDVMP",
"Proton vs. Electron Theta Angle, DVPP Candidates","Proton Angle","Electron Angle",
1,0,0,0,0,0)
type3 = ("output_file_histos_Hist_heleTheta",
"Electron Angle (Theta), with Proton Coincidence","Electron Angle","Counts",
0,0,0,0,0,0)
type4 = ("output_file_histos_Hist_hproTheta","Proton Angle (Theta)","Proton Angle","Counts",
0,0,0,110000,0,0)
type5 = ("output_file_histos_Hist_LeptHadAngle",
"Angle Between Lepton and Hadron Planes","Angle","Counts",
0,0,90,0,0,0)
type6 = ("output_file_histos_Hist_hproThetaCD",
"Proton Angle (Theta) in CD (Status > 4000)","Angle","Counts",
0,0,0,300,0,0)
type7 = ("output_file_histos_Hist_hproThetaFD",
"Proton Angle (Theta) in FD (2000 < Status < 4000)","Angle","Counts",
0,0,0,300,0,0)


Dtype8 = ("output_file_histos_Hist_hproThetaFD",
"Proton Angle (Theta) in FD and in CD","Angle","Counts",
0,0,0,200000,1,"output_file_histos_Hist_hproThetaCD")

Dtype9 = ("output_file_histos_Hist_hproThetaFDaftercuts",
"Proton Angle (Theta) in FD and in CD after Excl. Cuts","Angle","Counts",
0,0,0,300,1,"output_file_histos_Hist_hproThetaCDaftercuts")


# #plots = [type1,type9]
# #plots = [type1,type2,type3,type4,type5,type6,type7,Dtype8]
# plots = [type6,type7,Dtype8,Dtype9]


# os.mkdir("plots/"+zzz)
# os.mkdir("plots/"+zzz+"/original_python_pdfs")

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
