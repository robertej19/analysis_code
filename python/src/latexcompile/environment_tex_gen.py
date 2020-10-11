
def generate_environment_tex():
	preamble = r"""
\documentclass{article}
\usepackage[utf8]{inputenc}
\usepackage{graphicx}

\usepackage{caption}
\usepackage[margin=1in]{geometry}

\DeclareCaptionLabelFormat{blank}{}


\usepackage{array}
\usepackage{pdflscape}

\usepackage{color}
\usepackage{hyperref}
\hypersetup{
    colorlinks=true,
    citecolor=black,
    filecolor=black,
    linkcolor=blue,
    urlcolor=black
}

\usepackage{fancyhdr}
\pagestyle{fancy}
\fancyhead{} % clear all header fields
\renewcommand{\headrulewidth}{0pt} % no line in header area
\fancyfoot{} % clear all footer fields
%\fancyfoot[LE,RO]{\thepage}           % page number in "outer" position of footer line
\fancyfoot[C]{ \quad \quad \hyperlink{page.2}{o}}

	"""


	start_doc = r"""
\begin{document}



\section{Run Summary}

	"""

	end_doc = r"""
	\end{landscape}
	\end{document}
	"""

	start = preamble + start_doc


	startx = r"""
\documentclass{article}
\usepackage[utf8]{inputenc}
\usepackage{graphicx}
\graphicspath{ {figures/} }
\usepackage{array}




\begin{document}

\listoffigures


\newpage

\pagenumbering{arabic}

\begin{figure}
    \caption{Caption}
\end{figure}

\newpage

\begin{figure}
    \caption{Caption number 2}
\end{figure}


Lorem ipsum dolor sit amet, consectetuer adipiscing 
elit.  Etiam lobortisfacilisis...
\end{document}
"""

	return start, end_doc
