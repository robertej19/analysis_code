import sys
sys.argv.append('-b')
import os, subprocess
from pdf2image import convert_from_path
from PIL import Image
import math



#pdf_dir = "./plots/new_data_2020/"
#os.chdir(pdf_dir)

def img_from_pdf(pdf_dir,scaling_factor):
	fact = math.sqrt(float(scaling_factor))
	os.mkdir(pdf_dir+"../jpegs/")
	os.mkdir(pdf_dir+"../lowres/")
	for pdf_file in os.listdir(pdf_dir):
		print("On file " + pdf_file)
		names = pdf_file.split("T")[1]
		name = names.split(".pdf")[0]
		name = name.replace(" ", "_")
		name = name.replace("<", "_")
		pages = convert_from_path(pdf_dir+pdf_file, 600)


		for page in pages:
			temp_jpg = pdf_dir+"/"+name+'.jpg'
			cropped_jpg = pdf_dir+"../jpegs/"+"cropped"+name+'.jpg'
			lowres_pdf = pdf_dir+"../lowres/"+"cropped"+name+'.pdf'

			page.save(temp_jpg, 'JPEG')
			img = Image.open(temp_jpg)
			#img.crop((2200, 120, 6900, 3180)).save(cropped_jpg, quality=100) #this is for standard file cuts
			img.crop((2350, 400, 6350, 2840)).save(cropped_jpg, quality=100) #this is for t files
			os.remove(temp_jpg)

			image1 = Image.open(cropped_jpg)
			image1 = image1.resize((round(image1.size[0]/fact),round(image1.size[1]/fact)),Image.ANTIALIAS)
			image1.save(lowres_pdf,optimize=True,quality=95)


pdf_dir = sys.argv[1]
scaling_factor = sys.argv[2]

img_from_pdf(pdf_dir,scaling_factor)
