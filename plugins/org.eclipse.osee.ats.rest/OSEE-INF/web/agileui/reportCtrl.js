/**
 * Agile Reports Controller
 */
angular
		.module('AgileApp')
		.controller(
				'ReportCtrl',
				[
						'$scope',
						'AgileEndpoint',
						'$resource',
						'$window',
						'$modal',
						'$filter',
						'$routeParams',
						'LayoutService',
						'PopupService',
						function($scope, AgileEndpoint, $resource, $window,
								$modal, $filter, $routeParams, LayoutService,
								PopupService) {

							$scope.team = {};
							$scope.team.id = $routeParams.team;
							$scope.reporttype = $routeParams.reporttype;
							$scope.reportname = $routeParams.reportname;

							$scope.updateReports = function() {
								AgileEndpoint.getTeamSingle($scope.team).$promise
										.then(function(data) {
											$scope.team.name = data.name;
											$scope.sprint = {};
											$scope.sprint.id = data.sprintId;
		
											var htmlcontent = $('#b1 ');
											var url = "<object data=\"/ats/agile/team/";
											url = url
													.concat($scope.team.id);
											url = url
													.concat("/sprint/");
											url = url
													.concat($scope.sprint.id);
											if ($scope.reporttype == "burndown") {
												url = url
														.concat("/burndown/chart/ui?type=best\">");
											} else if ($scope.reporttype == "burnup") {
												url = url
														.concat("/burnup/chart/ui?type=best\">");
											} else if ($scope.reporttype == "data") {
												url = url
														.concat("/data/table?type=best\">");
											} else if ($scope.reporttype == "summary") {
												url = url
														.concat("/summary?type=best\">");
											}
		
											$("#b1").html(url);
		
											AgileEndpoint
													.getSprint(
															$scope.team.id,
															$scope.sprint.id).$promise
													.then(function(
															data) {
														$scope.reportname = data.name
																.concat(
																		" - ")
																.concat(
																		$scope.reportname)
																.concat(
																		" - ")
																.concat(
																		new Date()
																				.toLocaleString());
													}).catch((err) => {
														alert(err);
													});
										}).catch((err) => {
											alert(err);
										});
							}

							$scope.updateReports();

							// COMMON MENU COPIED TO ALL JS
							$scope.openConfigForTeam = function(team) {
								window.location.assign("main#/config?team="
										.concat($scope.team.id))
							}

							$scope.openKanbanForTeam = function(team) {
								window.location.assign("main#/kanban?team="
										.concat($scope.team.id))
							}

							$scope.openBurndownForTeam = function(team) {
								window.location.assign("main#/report?team="
										.concat($scope.team.id).concat("&reporttype=burndown&reportname=Burn-Down"))
							}

							$scope.openBurnupForTeam = function(team) {
								window.location.assign("main#/report?team="
										.concat($scope.team.id).concat("&reporttype=burnup&reportname=Burn-Up"))
							}

							$scope.openBacklogForTeam = function(team) {
								window.location.assign("main#/backlog?team="
										.concat($scope.team.id).concat("&default=backlog"))
							}

							$scope.openNewActionForTeam = function(team) {
								window.location.assign("main#/newAction?team="
										.concat($scope.team.id))
							}

							$scope.openSprintForTeam = function(team) {
								window.location.assign("main#/sprint?team="
										.concat($scope.team.id).concat("&default=sprint"))
							}

							$scope.openSummaryForTeam = function(team) {
								window.location.assign("main#/report?team="
										.concat($scope.team.id).concat("&reporttype=summary&reportname=Summary"))
							}

							$scope.openDataForTeam = function(team) {
								window.location.assign("main#/report?team="
										.concat($scope.team.id).concat("&reporttype=data&reportname=Data"))
							}
							// COMMON MENU COPIED TO ALL JS

						} ]);
