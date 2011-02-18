loadSource('HelloWorld2.js');
describe('HelloWorld',function(){
	it('should say hello again',function(){
		var helloWorld = new HelloWorld();
		expect(helloWorld.greeting()).toBe("Hello, World again!");
	});
});