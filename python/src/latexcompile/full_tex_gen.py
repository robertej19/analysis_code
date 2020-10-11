import os
import subprocess
import environment_tex_gen
from os import listdir
from os.path import isfile, join
import sys

start, end = environment_tex_gen.generate_environment_tex()

#csv_title = "particle_log.csv"

#cards = generate_particle_cards.generate_particle_cards(csv_title)


#pdf_location = "/mnt/c/Users/rober/Dropbox/Bobby/Linux/work/CLAS12/mit-clas12-analysis/theana/analysis_code/python/plots/output_file_histos-20201001-02-59/original_python_pdfs" 
#pdf_location = "/mnt/c/Users/rober/Dropbox/Bobby/Linux/work/CLAS12/mit-clas12-analysis/theana/analysis_code/python/plots/output_file_histos-20201001-02-59/textest" 

pdf_location = sys.argv[1]
#text_file_location = 'examplefile.txt'
text_file_location = sys.argv[2]

data = ""
with open(text_file_location,'r') as f_in:
    line = f_in.readline()
    while line:
        data += line + r"""\\"""
        line = f_in.readline()


data = data.replace("%","\%")
data = data.replace("_","\_")

print(data)



mypath = pdf_location
onlyfiles = [f for f in listdir(mypath) if isfile(join(mypath, f))]

midtex = r"""
\listoffigures
\clearpage
\begin{landscape}
"""

import json

with open('../dict_to_json_textfile.json') as fjson:
  histogram_file_title_mapping = json.load(fjson)

print(histogram_file_title_mapping)

for histname in onlyfiles:
    texstring = r"""
    \begin{figure}[h]
        \centering

        \includegraphics[scale=1.1]{"""

#STARTING NOW 
    picname = pdf_location+"/"+histname

    caption = histname.replace("_"," ")
    caption = caption.replace(".pdf", " ")

    caption_name = histogram_file_title_mapping[histname]
    caption_name = caption_name.replace("&","\&")
    caption_name = caption_name.replace("#gamma","$\gamma$")
    caption_name = caption_name.replace("#","FIX THIS NUMBERING")
    caption_name = caption_name.replace("^{2}","$^{2}$")
    


    #endstring = r"""}
     #   \label{fig:"""

    endstring = r"""}
        \captionsetup{textformat=empty,labelformat=blank}
        \caption{"""  

    endstring2 = r"""}
    \end{figure}
    \clearpage
    """

    picstring = texstring + picname + endstring + caption_name + endstring2
    midtex += picstring




final_text = start+data + "\clearpage " +midtex+ end
#final_text = start+ midtex+ end

#final_text = start
with open('latexoutput.tex','w') as f:
	f.write(final_text)

cmd = ['pdflatex','-interaction', 'nonstopmode','latexoutput.tex']
#cmd = ['pdflatex','--interaction=batchmode','latexoutput.tex','2>&1 > /dev/null']

proc = subprocess.Popen(cmd)
proc.communicate()
proc = subprocess.Popen(cmd)
proc.communicate()

retcode = proc.returncode
#if not retcode == 0:
#    os.unlink('latexoutput.pdf')
#    raise ValueError('Error {} executing command: {}'.format(retcode, ' '.join(cmd)))

print(retcode)
#os.unlink('latexoutput.tex')
#os.unlink('latexoutput.log')

""" Move the output files into their own directory"""

#print("PDF LOCATION IS{}".format(pdf_location))



#cmd = ['/home/bobby/bin/wsl-open.sh','latexoutput.pdf']
#proc = subprocess.Popen(cmd)
#proc.communicate()

