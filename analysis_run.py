#!/usr/bin/python

import sys
import subprocess
import os
import json
from datetime import datetime

now = datetime.now()
dt_string = now.strftime("%Y%m%d-%H-%M")
output_base_name = "output_file_histos-"+dt_string

this_file_path_original = os.path.dirname(os.path.abspath(__file__))

this_file_path = this_file_path_original+"/groovy/src"

os.chdir("groovy/src")

print(this_file_path)
#If not exists, create output file directory

output_path = "/../hipo-root-files/"+output_base_name
output_folder= this_file_path+output_path

#output_file_histos-20201011-01-43.hipo 


if os.path.isdir(output_folder):
	print('removing previous database file')
	subprocess.call(['rm','-rf',output_folder])
else:
	print(output_folder+" is not present, not deleteing")

subprocess.call(['mkdir','-p',output_folder])
print(output_folder+" is now present")


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
number_of_events = "100"
number_of_files = "1"
number_of_cores = "1"
run_message = "no message here"
output_folder_groovy = output_base_name


print("in ")

run_command = [run_groovy, groovy_script, data_location, number_of_events, number_of_files, number_of_cores, output_folder_groovy, run_message]

f_out = open(output_folder+"/"+runlog_filename, "w")
subprocess.call(run_command,stdout=f_out) #pipe commands to file output runlog

print("finished running analysis, now trying to do other stuff")

#***********************************

#******************
#********** Look at txt and hipo files


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

subprocess.call(run_command)


#Do Latex thing

print("trying to generate all plots")


os.chdir("latexcompile")

run_python = "python"
python2 = "full_tex_gen.py"
plots_path = this_file_path_original+"/python/plots/"+output_base_name+"/original_python_pdfs/"

run_command = [run_python,python2,plots_path]

subprocess.call(run_command)





