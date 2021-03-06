var fileApp = angular.module('fileApp', ['ngRoute', 'defaultServices',
  'menuControllers', 'defaultControllers'
]);

fileApp.config(function($routeProvider) {
  $routeProvider
    .when('/home', {
      controller: 'HomeCtrl',
      templateUrl: 'templates/home/home.html'
    }).when('/content', {
      controller: 'ContentCtrl',
      templateUrl: 'templates/content/content.html'
    }).when('/web/content', {
        controller: 'WebContentCtrl',
        templateUrl: 'templates/web/content/content.html'
    }).when('/web/wiki', {
        controller: 'WebWikiCtrl',
        templateUrl: 'templates/web/wiki/wiki.html'
    }).when('/scripting/javascript', {
      controller: 'JavaScriptCtrl',
      templateUrl: 'templates/scripting/javascript/javascript.html'
    }).when('/scripting/ruby', {
      controller: 'RubyCtrl',
      templateUrl: 'templates/scripting/ruby/ruby.html'
    }).when('/scripting/tests', {
        controller: 'TestsCtrl',
        templateUrl: 'templates/scripting/tests/tests.html'
    }).when('/routes', {
        controller: 'RoutesCtrl',
        templateUrl: 'templates/routes/routes.html'
    }).when('/monitoring', {
        controller: 'MonitoringCtrl',
        templateUrl: 'templates/monitoring/monitoring.html'
    }).when('/monitoring/manage', {
        controller: 'MonitoringManageCtrl',
        templateUrl: 'templates/monitoring/manage/manage.html'
    }).when('/monitoring/hits', {
        templateUrl: 'templates/monitoring/hits/hits.html'
    }).when('/monitoring/response', {
        templateUrl: 'templates/monitoring/response/response.html'
    }).when('/monitoring/memory', {
        templateUrl: 'templates/monitoring/memory/memory.html'
    }).when('/monitoring/acclog', {
    	controller: 'MonitoringAccessCtrl',
        templateUrl: 'templates/monitoring/acclog/acclog.html'
    }).when('/monitoring/logging', {
        templateUrl: 'templates/monitoring/logging/logging.html'
    }).otherwise({
      redirectTo: '/home'
    });
});

var menuControllers = angular.module('menuControllers', []);

menuControllers.controller('MenuCtrl', ['$scope', '$http',
  function($scope, $http) {
    $http.get('menu.json').success(function(data) {
      $scope.menus = data;
    });
  }
]);

menuControllers.controller('UserCtrl', ['$scope', '$http',
  function($scope, $http) {
    $scope.name = "Unknown";
    $http.get('op?user').success(function(data) {
      $scope.name = data;
    });
  }
]);

menuControllers.controller('HomeCtrl', ['$scope',
  function($scope) {
    $scope.homeData = [{
      image: "images/content.png",
      path: "#/content",
      title: "Repository",
      description: "Browse Artifacts in Repository"
    }, {
      image: "images/web-content.png",
      path: "#/web/content",
      title: "Web",
      description: "Browse Applications User Interfaces"
    }, {
      image: "images/wiki-content.png",
      path: "#/web/wiki",
      title: "Wiki",
      description: "Browse Applications Documentation"
    }, {
        image: "images/routes.png",
        path: "#/routes",
        title: "Routes",
        description: "Integration Services Endpoints"
    }, {
      image: "images/java-script.png",
      path: "#/scripting/javascript",
      title: "JavaScript",
      description: "JavaScript Services Endpoints"
    }, {
      image: "images/java-ruby.png",
      path: "#/scripting/ruby",
      title: "Ruby",
      description: "Ruby Services Endpoints"
    }, {
        image: "images/test.png",
        path: "#/scripting/tests",
        title: "Tests",
        description: "Test Cases Endpoints"
    }, {
      image: "images/monitor.png",
      path: "#/monitoring",
      title: "Monitoring",
      description: "Monitor Basic Metrics"
    }, {
      image: 'images/samples.png',
      path: 'http://samples.dirigible.io',
      title: 'Samples',
      description: "Browse Samples Space",
      newTab: true
    }, {
      image: 'images/help.png',
      path: 'http://help.dirigible.io',
      title: 'Help',
      description: "Browse Help Portal",
      newTab: true
    }, {
      image: 'images/about.png',
      path: 'http://www.dirigible.io',
      title: 'About',
      description: "Project Home Page",
      newTab: true
    }];

    $scope.getTarget = function(data) {
      return data.newTab ? '_blank"' : '_self';
    };
  }
]);

menuControllers.controller('MonitoringCtrl', ['$scope',
	function($scope) {
	$scope.monitoringData = [ {
	  image: "images/hits.png",
	  path: "#/monitoring/hits",
	  title: "Hits",
	  description: "Hit count graphics"
	}, {
	  image: "images/memory.png",
	  path: "#/monitoring/memory",
	  title: "Memory",
	  description: "Memory graphics"
	}, {
	  image: "images/response.png",
	  path: "#/monitoring/response",
	  title: "Response",
	  description: "Response time graphics"
	},{
	  image: "images/manage.png",
	  path: "#/monitoring/manage",
	  title: "Manage",
	  description: "Manage access locations"
	}, {
	  image: "images/access.png",
	  path: "#/monitoring/acclog",
	  title: "Access Log",
	  description: "Access Log"
	}, {
	  image: "images/logging.png",
	  path: "#/monitoring/logging",
	  title: "Logging",
	  description: "Logging"
	}];
  }
 ]);

menuControllers.controller('MonitoringManageCtrl', ['$scope', '$http',
  function($scope, $http) {
    var accessLogUrl = "/dirigible/acclog";
    $scope.locations = null;
    $scope.newLocation;

    loadData();

    function loadData() {
      $http.get(accessLogUrl + "/locations").success(function(result) {
        $scope.locations = result;
      }).error(function(data) {
        alert('Could not fetch access log data!');
      });
    }

    $scope.remove = function(location) {
      $http.delete(accessLogUrl + location)
        .success(function(result) {
          loadData();
        }).error(function(data) {
          alert('Error while removing location!');
        });
    };

    $scope.addNewLocation = function() {
      $http.post(accessLogUrl + $scope.newLocation).success(function(result) {
        loadData();
      }).error(function(data) {
        alert('Unable to add location ' + '"' + $scope.newLocation + '"' +
          '\nLocation must be in "project/index.html" format!');
      });
    };
  }
]);