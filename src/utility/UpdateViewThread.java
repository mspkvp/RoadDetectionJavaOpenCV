package utility;

class UpdateViewThread extends Thread {
	
	private Window w;
	private VideoCap v;
	
	public UpdateViewThread(Window w, VideoCap v) {
		this.w = w;
		this.v = v;
	}
	
	@Override
    public void run() {
        for (;;){
        	w.setImage(v.getOneFrame(false));
            w.repaint();
            try { 
            	Thread.sleep(15);
            } catch (InterruptedException e) {
            	e.printStackTrace();
            	break;
            }
        }  
    }
	
	@Override
	public synchronized void start() {
		if(w != null && v != null)
			super.start();
	}
}
