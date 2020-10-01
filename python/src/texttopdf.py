# from fpdf import FPDF 
# pdf = FPDF()      
# # Add a page 
# pdf.add_page()  
# # set style and size of font  
# # that you want in the pdf 
# pdf.set_font("Arial", size = 15)
# # open the text file in read mode 

# f = open("sampletxt.txt", "r") 

# # insert the texts in pdf 
# for x in f: 
#     pdf.cell(0,20, txt = x, ln = 1, align = 'C') 
# # save the pdf with name pdf 
# pdf.output("t2extpdfoutput.pdf")

from reportlab.platypus import SimpleDocTemplate, Paragraph
from reportlab.lib.styles import getSampleStyleSheet
from reportlab.lib.units import inch
from reportlab.lib.pagesizes import letter


styles = getSampleStyleSheet()
styleN = styles['Normal']
styleH = styles['Heading1']
story = []

file_loc = ""
#file_in_base = "output_file_histos-20200929-07-50"
file_in_base = "output_file_histos-20200930-06-43"
#file_in_base = "output_file_histos-20200927-13-54"
#../../groovy/hipo-root-files/output_file_histos-20200930-06-43.hipo.root

file_in = file_loc + file_in_base + ".txt"
pdf_name = "aaa"+file_in_base + ".pdf"

doc = SimpleDocTemplate(
    pdf_name,
    pagesize=letter,
    bottomMargin=.4 * inch,
    topMargin=.6 * inch,
    rightMargin=.8 * inch,
    leftMargin=.8 * inch)



with open(file_in, "r") as txt_file:
    text_content = txt_file.read()

textarray = text_content.split("\n")

for sentence in textarray:
    P = Paragraph(sentence, styleN)
    story.append(P)

doc.build(
    story,
)
