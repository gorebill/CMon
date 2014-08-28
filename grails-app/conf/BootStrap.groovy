
class BootStrap {
	
	def grailsApplication;

    def init = { servletContext ->
		
		grailsApplication.config.cmjob.isRunning=true;//TODO: read from database
		
    }
	
    def destroy = {
    }
}
