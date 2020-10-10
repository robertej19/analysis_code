import os
import subprocess
import environment_tex_gen
from os import listdir
from os.path import isfile, join


start, end = environment_tex_gen.generate_environment_tex()

#csv_title = "particle_log.csv"

#cards = generate_particle_cards.generate_particle_cards(csv_title)


pdf_location = "/mnt/c/Users/rober/Dropbox/Bobby/Linux/work/CLAS12/mit-clas12-analysis/theana/analysis_code/python/plots/output_file_histos-20201001-02-59/original_python_pdfs" 
#pdf_location = "/mnt/c/Users/rober/Dropbox/Bobby/Linux/work/CLAS12/mit-clas12-analysis/theana/analysis_code/python/plots/output_file_histos-20201001-02-59/textest" 


mypath = pdf_location
onlyfiles = [f for f in listdir(mypath) if isfile(join(mypath, f))]

midtex = ""

for histname in onlyfiles:
    texstring = r"""
    \begin{figure}[h]
        \centering

        \includegraphics[scale=0.6]{"""

#STARTING NOW 
    picname = pdf_location+"/"+histname

    caption = histname.replace("_"," ")
    caption = caption.replace(".pdf", " ")

    caption_name = caption+"]{~"

    #endstring = r"""}
     #   \label{fig:"""

    endstring = r"""}
        \captionsetup{textformat=empty,labelformat=blank}
        \caption{"""  

    endstring2 = r"""}
    \end{figure}
    \clearpage
    """

    picstring = texstring + picname + endstring + caption + endstring2
    midtex += picstring




final_text = start+"cars and bars" + midtex+ end
#final_text = start
with open('cards.tex','w') as f:
	f.write(final_text)

#cmd = ['pdflatex','-interaction', 'nonstopmode','cards.tex']
cmd = ['pdflatex','--interaction=batchmode','cards.tex','2>&1 > /dev/null']

proc = subprocess.Popen(cmd)
proc.communicate()

retcode = proc.returncode
if not retcode == 0:
    os.unlink('cards.pdf')
    raise ValueError('Error {} executing command: {}'.format(retcode, ' '.join(cmd)))

os.unlink('cards.tex')
os.unlink('cards.log')

""" Move the output files into their own directory"""


cmd = ['/home/bobby/bin/wsl-open.sh','cards.pdf']
#proc = subprocess.Popen(cmd)
#proc.communicate()
