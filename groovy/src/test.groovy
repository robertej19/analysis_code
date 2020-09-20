import groovy.json.JsonSlurper


filename = "../../histogram_dict.json"
 
def jsonSlurper = new JsonSlurper()
def data = jsonSlurper.parse(new File(filename))
 

for (hist in data){
    println("THE HIST IS")
    def hist_params = (hist.getValue()[0])

    def root_title= hist_params.get("root_title")
    def display_title = hist_params.get("display_title")
    def num_bins_x = hist_params.get("num_bins_x")
    def x_bin_min = hist_params.get("x_bin_min")
    def x_bin_max = hist_params.get("x_bin_max")
    def num_bins_z = hist_params.get("num_bins_z")
    def z_bin_min = hist_params.get("z_bin_min")
    def z_bin_max = hist_params.get("z_bin_max")


    if (num_bins_z > 0){
        def x2 = new H2F(root_title, display_title, num_bins_x, x_bin_min, x_bin_max, num_bins_z, z_bin_min, z_bin_max)
    }
    else {
        def x1 = new H1F(root_title, display_title, num_bins_x, x_bin_min, x_bin_max)
    }

    println(num_bins)
    
}
