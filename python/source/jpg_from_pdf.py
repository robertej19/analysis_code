import os, subprocess
from pdf2image import convert_from_path
from PIL import Image

pdf_dir = "./plots/cd_fd_protons/"
#os.chdir(pdf_dir)


for pdf_file in os.listdir(pdf_dir):
	print("On file " + pdf_file)
	name = pdf_file.split(".")[0]
	pages = convert_from_path(pdf_dir+pdf_file, 600)
	for page in pages:
	    page.save(pdf_dir+"/"+name+'.jpg', 'JPEG')
	    img = Image.open(pdf_dir+"/"+name+'.jpg')
	    img.crop((2200, 120, 6900, 3180)).save(pdf_dir+"/"+"cropped"+name+'.jpg', quality=100)
