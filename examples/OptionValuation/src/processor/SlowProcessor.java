package processor;


public class SlowProcessor extends Processor{

	protected int delay() {
		return 1000;
	}

}
