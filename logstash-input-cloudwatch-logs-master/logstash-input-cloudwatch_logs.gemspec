Gem::Specification.new do |s|

  s.name            = 'logstash-input-cloudwatch_logs'
  s.version         = '1.0.4'
  s.licenses        = ['Apache-2.0']
  s.summary         = 'Stream events from CloudWatch Logs.'
  s.description     = 'This gem is a logstash plugin required to be installed on top of the Logstash core pipeline using $LS_HOME/bin/plugin install gemname. This gem is not a stand-alone program'
  s.authors         = ['IBM']
  s.email           = ''
  s.homepage        = 'https://github.ibm.com/Activity-Insights/universal-connector/tree/master/logstash-input-cloudwatch-logs-master'
  s.require_paths   = ['lib']

  # Files
  s.files = Dir['lib/**/*','spec/**/*','vendor/**/*','*.gemspec','*.md','CONTRIBUTORS','Gemfile','LICENSE','NOTICE.TXT']

  # Tests
  s.test_files = s.files.grep(%r{^(test|spec|features)/})

  # Special flag to let us know this is actually a logstash plugin
  s.metadata = { 'logstash_plugin' => 'true', 'logstash_group' => 'input' }

  # Gem dependencies
  s.add_runtime_dependency 'logstash-core-plugin-api', '>= 2.1.12', '<= 2.99'
  s.add_runtime_dependency 'stud', '~> 0.0.22'
  s.add_runtime_dependency "aws-sdk-core", "~> 3", ">= 3.165.0"
  s.add_runtime_dependency "aws-sdk-cloudwatch", "~> 1"
  s.add_runtime_dependency "aws-sdk-resourcegroups", "~> 1"
  s.add_runtime_dependency "aws-sdk-configservice", "~> 1"
  s.add_runtime_dependency "aws-sdk-cloudwatchlogs", "~> 1"

  s.add_development_dependency "logstash-devutils", "~> 2"


end
