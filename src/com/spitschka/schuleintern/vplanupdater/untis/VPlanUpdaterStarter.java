package com.spitschka.schuleintern.vplanupdater.untis;

public class VPlanUpdaterStarter {

	private UpdateThread updater;
	
	public VPlanUpdaterStarter() {
		updater = new UpdateThread();
		updater.start();
	}

	public static void main(String[] args) {
		VPlanUpdater.init();
		
			
		new VPlanUpdaterStarter();
	}

}
