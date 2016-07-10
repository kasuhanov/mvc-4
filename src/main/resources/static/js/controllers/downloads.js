app.controller('DownloadsController', function($scope, $state, $http, $rootScope, $window, fileUpload){
    $rootScope.title = "Загрузки";
    if($rootScope.authority != "ROLE_ADMIN"){
        $state.transitionTo('home');
    }
    init();
    
    function init() {
        $http.get('admin/first-and-last-purchases-dates')
            .success(function (data) {
                $scope.first = data.first;
                $scope.last = data.last;
            });
        $scope.loading_downloads = true;
        $http.get('admin/downloads')
            .success(function (data) {
                $scope.loading_downloads = false;
                $scope.downloads = data;
            });
    }

    $scope.download = function(){
        $scope.loading = true;
    	$http.get('admin/download')
	    	.success(function () {
	            $window.location.reload();
	        })
	        .error(function () {
	            $scope.downloadFailed = true;
                $scope.loading = false;
	        });
    };

    $scope.toggleArch = function(download){
        if(download.showArch === undefined){
            download.showArch = true;
            download.loading_archives = true;
            $http.get('admin/download/'+download.id+'/archives')
                .success(function (data) {
                    download.ftpArchives = data;
                    download.loading_archives = false;
                });
        }
        else
            download.showArch = !download.showArch;
    };

    $scope.toggleXml = function(archive){
        if(archive.showXml === undefined){
            archive.showXml = true;
            $http.get('admin/archive/'+archive.id+'/xml')
                .success(function (data) {
                    archive.xmlFiles = data;
                });
        }
        else
            archive.showXml = !archive.showXml;
    };

    $scope.calendar = {
        open: false
    };

    $scope.open = function() {
        $scope.calendar.open = true;
    };

    $scope.submit = function(){
        $scope.loadingSinceDate = true;
        var day = 24 * 60 * 60 * 1000;
        $http.get('admin/download/date/'+($scope.date.getTime() - day))
            .success(function () {
                $window.location.reload();
                $scope.loadingSinceDate = false;
            });
    };

    $scope.upload = function(){
        var file = $scope.file;
        fileUpload.uploadFileToUrl(file, 'admin/upload',
            function(){
                $scope.uploadFailed = false;
                $window.location.reload();
            },
            function(){
                $scope.uploadFailed = true;
            });
    };

    $scope.notify = function(){
        $scope.notifing = true;
        $http.get('admin/notify')
            .success(function () {
                $scope.notifing = false;
            });
    };

    $scope.reloadArchive = function(archive){
        archive.loading = true;
        archive.reloadSuccess = false;
        archive.reloadFailed = false;
        $http.get('admin/archive/'+archive.id+'/reload')
            .success(function (data) {
                archive.id = data.id;
                archive.message = data.message;
                archive.status = data.status;
                archive.failed = data.failed;
                archive.succeeded = data.succeeded;
                archive.loading = false;
                archive.reloadSuccess = true;
            })
            .error(function () {
                archive.loading = false;
                archive.reloadFailed = true;
            });
    };

    $scope.reloadXml = function(xml){
        xml.loading = true;
        xml.reloadSuccess = false;
        xml.reloadFailed = false;
        $http.get('admin/xml/'+xml.id+'/reload')
            .success(function (data) {
                xml.message = data.message;
                xml.status = data.status;
                xml.loading = false;
                xml.reloadSuccess = true;
            })
            .error(function () {
                xml.loading = false;
                xml.reloadFailed = true;
            });
    };
});
