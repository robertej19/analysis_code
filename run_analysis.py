#!/usr/bin/python

import sys
import subprocess
import os
import json
from datetime import datetime
import shutil

now = datetime.now()
dt_string = now.strftime("%Y%m%d-%H-%M")
output_base_name = "output_file_histos-"+dt_string

this_file_path_original = os.path.dirname(os.path.abspath(__file__))

this_file_path = this_file_path_original+"/groovy/src"

os.chdir("groovy/src")

#If not exists, create output file directory

output_path = "/../../analysis_outputs/"+output_base_name
output_folder= this_file_path+output_path

#output_file_histos-20201011-01-43.hipo 


if os.path.isdir(output_folder):
	print('removing previous database file')
	subprocess.call(['rm','-rf',output_folder])
else:
	print("outputdir is not present, not deleteing")

subprocess.call(['mkdir','-p',output_folder])
print("output dir is now present")


#******************


temp_output_filename_file = 'run_output_filename.txt'
runlog_filename = "analysis_runlog.txt"

#**********************
#Process groovy hipo events

#arg0 = "$COATJAVA/bin/run-groovy"

loc2outbending = "/mnt/d/CLAS12Data/skim4/skim4-20200927"
loc3fx = "/mnt/d/CLAS12Data/out"

run_groovy = "/home/bobby/theana-software/coatjava/coatjava/bin/run-groovy"
groovy_script = "main_ana.groovy"
data_location = "/mnt/d/CLAS12Data/skim8-20200629"
number_of_events = "0"
number_of_files = "20"
number_of_cores = "1"
run_message = "Note: some histograms are mislabled below due to a bug in the LaTex code, but this does not affect analysis and is only aesthetic"
output_folder_groovy = output_base_name


print("Running groovy analysis")

run_command = [run_groovy, groovy_script, data_location, number_of_events, number_of_files, number_of_cores, output_folder_groovy, run_message]

f_out = open(output_folder+"/"+runlog_filename, "w")
subprocess.call(run_command,stdout=f_out) #pipe commands to file output runlog

print("finished running analysis, now trying to do other stuff")

#***********************************

#******************
#********** Look at txt and hipo files

text_file_path = output_folder+"/"+output_base_name+".txt"


#subprocess.call(["/home/bobby/bin/wsl-open.sh",output_folder+"/"+output_base_name+".txt"]) #see text file
#subprocess.call(["java","-jar","groovy/src/TBrowser-1.0-jar-with-dependencies.jar",output_folder+"/"+output_base_name+".hipo"]) #see hipo file


#******************
#********** Convert hipo to root file

print("trying to convert from hipo to root")

run_groovy = "/home/bobby/theana-software/coatjava/coatjava/bin/run-groovy"
groovy_script = this_file_path+"/../scripts/hipo_to_root/j2root.groovy"
finished_hipo_file = output_folder+"/"+output_base_name+".hipo"

run_command = [run_groovy,groovy_script, finished_hipo_file]

f_out = open(output_folder+"/"+"conversion_log.txt", "w")
subprocess.call(run_command,stdout=f_out) #pipe commands to file output runlog


#******
#Generate root plots from root file
print("trying to generate all plots")


os.chdir("../../python/src")

run_python = "python2"
python2 = "gen_all_plots.py"
plots_path = output_folder+"/"+output_base_name+".hipo.root"



run_command = [run_python,python2,plots_path]


f_out = open(output_folder+"/"+"gen_allplots_log.txt", "w")
subprocess.call(run_command,stdout=f_out)


#Do Latex thing

print("trying to generate all plots in latex file")


os.chdir("latexcompile")

run_python = "python"
python2 = "full_tex_gen.py"
plots_path = output_folder+"/plots/original_python_pdfs/"


run_command = [run_python,python2,plots_path,text_file_path]

f_out = open(output_folder+"/"+"latex_log.txt", "w")
subprocess.call(run_command,stdout=f_out)

shutil.move(this_file_path_original+"/python/src/latexcompile"+"/latexoutput.pdf",   output_folder+"/"+"latexoutput.pdf")
shutil.move(this_file_path_original+"/python/src/latexcompile"+"/latexoutput.tex",   output_folder+"/"+"latexoutput.tex")
#shutil.move(this_file_path_original+"/python/src/latexcompile"+"/latexoutput.out",   output_folder+"/"+"latexoutput.out")

subprocess.call(["rm","latexoutput.aux"])
subprocess.call(["rm","latexoutput.lof"])
subprocess.call(["rm","latexoutput.out"])
subprocess.call(["rm","latexoutput.log"])


subprocess.call(["/home/bobby/bin/wsl-open.sh",output_folder+"/"+"latexoutput.pdf"])


#os.rename("latexoutput.pdf",  output_folder+"/"+"latexoutput.pdf")
#os.rename("latexoutput.tex",  output_folder+"/"+"latexoutput.tex")
#os.rename("latexoutput.out",  output_folder+"/"+"latexoutput.out")

#subprocess.call(["/home/bobby/bin/wsl-open.sh",output_folder+"/"+"latexoutput.log"]) #see text file