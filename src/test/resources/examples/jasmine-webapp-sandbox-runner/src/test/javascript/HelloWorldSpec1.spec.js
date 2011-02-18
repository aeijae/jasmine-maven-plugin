loadScript("../../src/main/javascript/HelloWorld.js");
describe('HelloWorld',function(){
	it('should say hello and concatenate variable from preloaded source',function(){
        var helloWorld = new HelloWorld();
		expect(helloWorld.greeting()).toBe("Hello, World from external source");
	});
});